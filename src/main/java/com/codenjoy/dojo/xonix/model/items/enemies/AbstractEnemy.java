package com.codenjoy.dojo.xonix.model.items.enemies;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2021 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.xonix.model.Elements;
import com.codenjoy.dojo.xonix.model.Field;
import com.codenjoy.dojo.xonix.model.items.AbstractItem;

import java.util.function.Function;

public abstract class AbstractEnemy extends AbstractItem implements Enemy {

    private final Function<Point, Boolean> barrierChecker;
    protected Direction direction;
    protected Field field;

    protected AbstractEnemy(Point pt, Elements element, Field field, Function<Point, Boolean> barrierChecker) {
        super(pt, element);
        this.field = field;
        this.barrierChecker = barrierChecker;
        this.direction = Direction.UP;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void tick() {
        if (direction == null) {
            return;
        }
        Point position = getPosition();
        int limiter = 4;
        while ((barrierChecker.apply(diagonalStep(position))
                || barrierChecker.apply(direction.change(position))
                || barrierChecker.apply(direction.clockwise().change(position))
                || field.isOutOfBounds(diagonalStep(position)))
                && limiter > 0) {
            if (barrierChecker.apply(direction.change(position))) {
                direction = direction.clockwise();
            } else if (barrierChecker.apply(direction.clockwise().change(position))) {
                direction = direction.counterClockwise();
            } else if (barrierChecker.apply(diagonalStep(position))) {
                direction = direction.inverted();
            } else {
                direction = direction.counterClockwise();
            }
            limiter--;
        }
        if (limiter != 0) {
            move(diagonalStep(position));
        }
    }

    private Point diagonalStep(Point point) {
        Point position = direction.change(point);
        switch (direction) {
            case LEFT:
                return Direction.UP.change(position);
            case UP:
                return Direction.RIGHT.change(position);
            case RIGHT:
                return Direction.DOWN.change(position);
            case DOWN:
                return Direction.LEFT.change(position);
            default:
                throw new IllegalStateException("Direction should be LEFT, RIGHT, UP or DOWN");
        }
    }
}