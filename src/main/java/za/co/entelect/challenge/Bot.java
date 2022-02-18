package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private final static Command FIX = new FixCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command OIL = new OilCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    public Command run() {
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        List<Object> nextBlocks = blocks.subList(0, 1);

        boolean isOpponentInFront = blocks.contains(opponent);
        boolean isMudInFront = blocks.contains(Terrain.MUD);
        boolean isOilSpillInFront = blocks.contains(Terrain.OIL_SPILL);
        boolean isWallInFront = blocks.contains(Terrain.WALL);

        if (myCar.damage >= 3) {
            return FIX;
        }

        if (myCar.speed == 0) {
            return new AccelerateCommand();
        }

        if (isMudInFront || isOilSpillInFront || isOpponentInFront || isWallInFront) {
            if (myCar.position.lane == 1) {
                // return TURN_RIGHT;
                
                List<Object> rightBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block);
                int oilSpillDistance = getDistanceToObject(blocks, Terrain.OIL_SPILL);
                int mudDistance = getDistanceToObject(blocks, Terrain.MUD);
                int opponentDistance = getDistanceToObject(blocks, opponent);
                int wallDistance = getDistanceToObject(blocks, Terrain.WALL);
                
                int rightOilSpillDistance = getDistanceToObject(rightBlocks, Terrain.OIL_SPILL);
                int rightMudDistance = getDistanceToObject(rightBlocks, Terrain.MUD);
                int rightOpponentDistance = getDistanceToObject(rightBlocks, opponent);
                int rightWallDistance = getDistanceToObject(rightBlocks, Terrain.WALL);

                // nearest obstacle nearby
                int nearestObstacleInFrontDistance = min(min(oilSpillDistance, mudDistance), min(opponentDistance, wallDistance));
                int nearestObstacleInRightDistance = min(min(rightOilSpillDistance, rightMudDistance), min(rightOpponentDistance, rightWallDistance));

                // decide to turn right or just decelerate
                if (nearestObstacleInFrontDistance < nearestObstacleInRightDistance && myCar.speed <= 3) {
                    return TURN_RIGHT;
                } else {
                    return new DecelerateCommand();
                }

            } else if (myCar.position.lane == 4) {
                // return TURN_LEFT;
                List<Object> leftBlocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block);

                int oilSpillDistance = getDistanceToObject(blocks, Terrain.OIL_SPILL);
                int mudDistance = getDistanceToObject(blocks, Terrain.MUD);
                int opponentDistance = getDistanceToObject(blocks, opponent);
                int wallDistance = getDistanceToObject(blocks, Terrain.WALL);

                int leftOilSpillDistance = getDistanceToObject(leftBlocks, Terrain.OIL_SPILL);
                int leftMudDistance = getDistanceToObject(leftBlocks, Terrain.MUD);
                int leftOpponentDistance = getDistanceToObject(leftBlocks, opponent);
                int leftWallDistance = getDistanceToObject(leftBlocks, Terrain.WALL);

                // nearest obstacle nearby
                int nearestObstacleInFrontDistance = min(min(oilSpillDistance, mudDistance), min(opponentDistance, wallDistance));
                int nearestObstacleInLeftDistance = min(min(leftOilSpillDistance, leftMudDistance), min(leftOpponentDistance, leftWallDistance));

                // decide to turn left or just decelerate
                if (nearestObstacleInFrontDistance < nearestObstacleInLeftDistance && myCar.speed <= 3) {
                    return TURN_LEFT;
                } else {
                    return new DecelerateCommand();
                }
                
            } else {
                List<Object> rightBlocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block);
                List<Object> leftBlocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block);
                
                int oilSpillDistance = getDistanceToObject(blocks, Terrain.OIL_SPILL);
                int mudDistance = getDistanceToObject(blocks, Terrain.MUD);
                int opponentDistance = getDistanceToObject(blocks, opponent);
                int wallDistance = getDistanceToObject(blocks, Terrain.WALL);

                int rightOilSpillDistance = getDistanceToObject(rightBlocks, Terrain.OIL_SPILL);
                int rightMudDistance = getDistanceToObject(rightBlocks, Terrain.MUD);
                int rightOpponentDistance = getDistanceToObject(rightBlocks, opponent);
                int rightWallDistance = getDistanceToObject(rightBlocks, Terrain.WALL);

                int leftOilSpillDistance = getDistanceToObject(leftBlocks, Terrain.OIL_SPILL);
                int leftMudDistance = getDistanceToObject(leftBlocks, Terrain.MUD);
                int leftOpponentDistance = getDistanceToObject(leftBlocks, opponent);
                int leftWallDistance = getDistanceToObject(leftBlocks, Terrain.WALL);

                // nearest obstacle nearby
                int nearestObstacleInFrontDistance = min(min(oilSpillDistance, mudDistance), min(opponentDistance, wallDistance));
                int nearestObstacleInRightDistance = min(min(rightOilSpillDistance, rightMudDistance), min(rightOpponentDistance, rightWallDistance));
                int nearestObstacleInLeftDistance = min(min(leftOilSpillDistance, leftMudDistance), min(leftOpponentDistance, leftWallDistance));

                // decide whether to turn left, right, or just decelerate
                // cases:
                // front > left && front > right -> decelerate
                // front < left && front < right -> turn right if left < right else turn left
                // left <= front && front < right -> turn right
                // left > front && front >= right -> turn left
                // left == front && front == right -> turn left if car is in lane 3 else turn right
                if (nearestObstacleInFrontDistance > nearestObstacleInLeftDistance && nearestObstacleInFrontDistance > nearestObstacleInRightDistance) {
                    return new DecelerateCommand();
                } else if (nearestObstacleInFrontDistance < nearestObstacleInLeftDistance && nearestObstacleInFrontDistance < nearestObstacleInRightDistance) {
                    if (myCar.position.lane == 3) {
                        return TURN_LEFT;
                    } else {
                        return TURN_RIGHT;
                    }
                } else if (nearestObstacleInLeftDistance <= nearestObstacleInFrontDistance && nearestObstacleInFrontDistance < nearestObstacleInRightDistance) {
                    return TURN_RIGHT;
                } else if (nearestObstacleInLeftDistance > nearestObstacleInFrontDistance && nearestObstacleInFrontDistance >= nearestObstacleInRightDistance) {
                    return TURN_LEFT;
                } else {
                    if (myCar.position.lane == 3) {
                        return TURN_LEFT;
                    } else {
                        return TURN_RIGHT;
                    }
                }
                
            }
        }

        // pake boost
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }        

        // kalo ngebut, tumpahin oli aja hehe
        if (hasPowerUp(PowerUps.OIL, myCar.powerups) && myCar.speed >= opponent.speed) {
            return OIL;
        }

        // lemahkan lawan
        if (myCar.speed >= 6) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
        }

        return new AccelerateCommand();
    }

    private int getDistanceToObject(List<Object> blocks, Object object) {
        int distance = 0;
        for (Object block : blocks) {
            if (block == object) {
                return distance;
            }
            distance++;
        }
        return 999;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}
