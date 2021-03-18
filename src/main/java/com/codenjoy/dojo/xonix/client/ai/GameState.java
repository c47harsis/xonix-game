package com.codenjoy.dojo.xonix.client.ai;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.xonix.client.Board;
import com.codenjoy.dojo.xonix.model.Elements;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameState {

    private final Board board;
    private final Point xonix;
    private final List<Point> trace;
    private final List<Point> sea;
    private final List<Point> land;
    private final List<Point> landEnemies;
    private final List<Point> marineEnemies;

    private final List<Point> dangerZone;

    public GameState(Board board) {
        this.board = board;
        xonix = board.getXonix();
        trace = board.getTrace();
        sea = board.getSea();
        land = board.getLand();
        landEnemies = board.getLandEnemies();
        marineEnemies = board.getMarineEnemies();
        dangerZone = determineDangerZone();
    }

    public Direction howToAvoidDanger() {
        return isXonixFloating() ? howToAvoidDangerAtSea() : howToAvoidDangerOnLand();
    }

    public Direction howToAvoidDangerOnLand() {
        Point enemy = landEnemies.stream()
                .filter(dangerZone::contains)
                .findAny().orElse(null);

        if (enemy == null) {
            return null;
        }

        int x = enemy.getX();
        int y = enemy.getY();

        List<Direction> possibleDirections;

        if (x < xonix.getX()) {
            if (y < xonix.getY()) {
                possibleDirections = Lists.newArrayList(Direction.RIGHT, Direction.UP);
            } else {
                possibleDirections = Lists.newArrayList(Direction.RIGHT, Direction.DOWN);
            }
        } else {
            if (y < xonix.getY()) {
                possibleDirections = Lists.newArrayList(Direction.LEFT, Direction.UP);
            } else {
                possibleDirections = Lists.newArrayList(Direction.LEFT, Direction.DOWN);
            }
        }

        for (Direction direction : possibleDirections) {
            if (!isLand(direction.change(xonix))) {
                Point fourSteps = direction.change(direction.change(direction.change(direction.change(xonix))));
                boolean canGoToSea = getPointsAround(fourSteps, 4).stream()
                        .filter(this::isInBounds)
                        .noneMatch(marineEnemies::contains);
                if (canGoToSea) {
                    return direction;
                }
            }
        }

        // if there is no way to stay on land
        return possibleDirections.get(
                new Random().nextInt(possibleDirections.size() - 1)
        );
    }

    public Direction howToAvoidDangerAtSea() {
        Point enemy = marineEnemies.stream()
                .filter(dangerZone::contains)
                .findAny().orElse(null);

        if (enemy == null) {
            return null;
        }

        for (Direction direction : Direction.getValues()) {
            if (isLand(direction.change(xonix))) {
                return direction;
            }
        }

        if (enemy.getX() > xonix.getX()) {
            if (enemy.getY() > xonix.getY()) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        } else {
            return Direction.RIGHT;
        }
    }

    private List<Point> getPointsAround(Point point, int areaSize) {
        List<Point> result = Lists.newArrayList(point);
        for (int i = 0; i < areaSize; i++) {
            result = result.stream()
                    .flatMap(p -> {
                        int x = p.getX();
                        int y = p.getY();
                        return Stream.of(
                                PointImpl.pt(x - 1, y),
                                PointImpl.pt(x - 1, y - 1),
                                PointImpl.pt(x - 1, y + 1),

                                PointImpl.pt(x + 1, y),
                                PointImpl.pt(x + 1, y - 1),
                                PointImpl.pt(x + 1, y + 1),

                                PointImpl.pt(x, y - 1),
                                PointImpl.pt(x - 1, y - 1),
                                PointImpl.pt(x + 1, y - 1),

                                PointImpl.pt(x, y + 1),
                                PointImpl.pt(x - 1, y + 1),
                                PointImpl.pt(x + 1, y + 1)
                        );
                    }).distinct()
                    .collect(Collectors.toList());
        }
        return result;
    }

    private List<Point> determineDangerZone() {
        ArrayList<Point> vulnerable = Lists.newArrayList(xonix);
        vulnerable.addAll(trace);
        return vulnerable.stream()
                .flatMap(p -> getPointsAround(p, 2).stream())
                .distinct()
                .filter(this::isInBounds)
                .filter(p -> isXonixFloating() ? isSea(p) : isLand(p))
                .collect(Collectors.toList());
    }

    public boolean isSea(Point point) {
        return sea.contains(point) || marineEnemies.contains(point) || trace.contains(point);
    }

    public boolean isLand(Point point) {
        return land.contains(point) || landEnemies.contains(point);
    }

    public boolean isInBounds(Point point) {
        return !board.isOutOfField(point.getX(), point.getY());
    }

    public Elements getAt(Point point) {
        return board.getAt(point);
    }

    public boolean isXonixFloating() {
        return sea.contains(xonix) || !trace.isEmpty();
    }

    public boolean isXonixOnLand() {
        return land.contains(xonix);
    }

    public Point getXonix() {
        return xonix;
    }

    public List<Point> getTrace() {
        return trace;
    }

    public List<Point> getSea() {
        return sea;
    }

    public List<Point> getLand() {
        return land;
    }

    public List<Point> getLandEnemies() {
        return landEnemies;
    }

    public List<Point> getMarineEnemies() {
        return marineEnemies;
    }
}