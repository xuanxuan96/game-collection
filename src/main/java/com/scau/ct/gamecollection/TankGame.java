package com.scau.ct.gamecollection;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TankGame extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TANK_SIZE = 50;
    private static final int ENEMY_SIZE = 40;
    private static final int BULLET_SIZE = 10;
    private static final double BULLET_SPEED = 3.0;
    private static final double ENEMY_SPEED = 1.0;

    private int tankX;
    private int tankY;
    private KeyCode lastKeyCode;

    private List<EnemyTank> enemyTanks;
    private List<Bullet> bullets;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        tankX = WIDTH / 2 - TANK_SIZE / 2;
        tankY = HEIGHT / 2 - TANK_SIZE / 2;

        enemyTanks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int x = (int) (Math.random() * (WIDTH - ENEMY_SIZE));
            int y = (int) (Math.random() * (HEIGHT - ENEMY_SIZE));
            enemyTanks.add(new EnemyTank(x, y, ENEMY_SIZE));
        }

        bullets = new ArrayList<>();

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyRelease);

        primaryStage.setTitle("Tank Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> Platform.exit());

        primaryStage.show();

        new Thread(() -> {
            while (true) {
                Platform.runLater(() -> drawGame(gc));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void drawGame(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.GREEN);
        gc.fillRect(tankX, tankY, TANK_SIZE, TANK_SIZE);

        gc.setFill(Color.RED);
        for (EnemyTank enemyTank : enemyTanks) {
            gc.fillRect(enemyTank.getX(), enemyTank.getY(), ENEMY_SIZE, ENEMY_SIZE);
        }

        gc.setFill(Color.BLACK);
        for (Bullet bullet : bullets) {
            gc.fillRect(bullet.getX(), bullet.getY(), BULLET_SIZE, BULLET_SIZE);
        }

        updateGame();
    }

    private void updateGame() {
        updateTankPosition();
        updateEnemyTanks();
        updateBullets();
        checkCollisions();
    }

    private void updateTankPosition() {
        if (lastKeyCode == KeyCode.UP && tankY > 0) {
            tankY -= 2;
        } else if (lastKeyCode == KeyCode.DOWN && tankY < HEIGHT - TANK_SIZE) {
            tankY += 2;
        } else if (lastKeyCode == KeyCode.LEFT && tankX > 0) {
            tankX -= 2;
        } else if (lastKeyCode == KeyCode.RIGHT && tankX < WIDTH - TANK_SIZE) {
            tankX += 2;
        }
    }

    private void updateEnemyTanks() {
        Random random = new Random();
        for (EnemyTank enemyTank : enemyTanks) {
            int direction = random.nextInt(4);
            if (direction == 0 && enemyTank.getY() > 0) {
                enemyTank.setY(enemyTank.getY() - 1);
            } else if (direction == 1 && enemyTank.getY() < HEIGHT - ENEMY_SIZE) {
                enemyTank.setY(enemyTank.getY() + 1);
            } else if (direction == 2 && enemyTank.getX() > 0) {
                enemyTank.setX(enemyTank.getX() - 1);
            } else if (direction == 3 && enemyTank.getX() < WIDTH - ENEMY_SIZE) {
                enemyTank.setX(enemyTank.getX() + 1);
            }
        }
    }

    private void updateBullets() {
        for (Bullet bullet : bullets) {
            if (bullet.getDirection() == Direction.UP && bullet.getY() > 0) {
                bullet.setY(bullet.getY() - BULLET_SPEED);
            } else if (bullet.getDirection() == Direction.DOWN && bullet.getY() < HEIGHT - BULLET_SIZE) {
                bullet.setY(bullet.getY() + BULLET_SPEED);
            } else if (bullet.getDirection() == Direction.LEFT && bullet.getX() > 0) {
                bullet.setX(bullet.getX() - BULLET_SPEED);
            } else if (bullet.getDirection() == Direction.RIGHT && bullet.getX() < WIDTH - BULLET_SIZE) {
                bullet.setX(bullet.getX() + BULLET_SPEED);
            }
        }
    }

    private void checkCollisions() {
        for (Bullet bullet : bullets) {
            for (EnemyTank enemyTank : enemyTanks) {
                if (bullet.intersects(enemyTank)) {
                    bullets.remove(bullet);
                    enemyTanks.remove(enemyTank);
                    break;
                }
            }
        }
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN ||
                event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT) {
            lastKeyCode = event.getCode();
        } else if (event.getCode() == KeyCode.SPACE) {
            int bulletX = tankX + TANK_SIZE / 2 - BULLET_SIZE / 2;
            int bulletY = tankY + TANK_SIZE / 2 - BULLET_SIZE / 2;
            Direction bulletDirection = Direction.fromKeyCode(lastKeyCode);
            bullets.add(new Bullet(bulletX, bulletY, BULLET_SIZE, bulletDirection));
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        if (event.getCode() == lastKeyCode) {
            lastKeyCode = null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

enum Direction {
    UP(KeyCode.UP),
    DOWN(KeyCode.DOWN),
    LEFT(KeyCode.LEFT),
    RIGHT(KeyCode.RIGHT);

    private final KeyCode keyCode;

    Direction(KeyCode keyCode) {
        this.keyCode = keyCode;
    }

    public static Direction fromKeyCode(KeyCode keyCode) {
        for (Direction direction : values()) {
            if (direction.keyCode == keyCode) {
                return direction;
            }
        }
        return null;
    }
}

class EnemyTank {
    private int x;
    private int y;
    private int size;

    public EnemyTank(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSize() {
        return size;
    }
}

class Bullet {
    private double x;
    private double y;
    private double size;
    private Direction direction;

    public Bullet(double x, double y, double size, Direction direction) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.direction = direction;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getSize() {
        return size;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean intersects(EnemyTank enemyTank) {
        return x + size >= enemyTank.getX() &&
                x <= enemyTank.getX() + enemyTank.getSize() &&
                y + size >= enemyTank.getY() &&
                y <= enemyTank.getY() + enemyTank.getSize();
    }
}
