'''
@author: Mathieu Plourde - mat.plourde@gmail.com
'''

import world as world_module
import utilities
import actions
import random

class MyGang(world_module.Gang):
    
    def __init__(self, port, name):
        super(MyGang, self).__init__(port, name)
    
    def compute(self, world):
        
        a = []
        
        for player in world.teams[self.teamId].players:
            
            # if no one holds the flag and the flag can be picked (can only be picked if 20% of the players are dead)
            if(world.flag.holder is None and world.canPickFlag):
                
                # if the player is close enough, we pick it, else we move to it. distance to pick flag is <= 10
                if(utilities.maths.getEuclidianDistance(player.x, player.y, world.flag.x, world.flag.y) <= actions.PickFlagAction.MIN_DISTANCE_TO_PICK):
                    a.append(actions.PickFlagAction(player))
                elif(not(player.playerState.stateType == world_module.StateType.Moving and player.playerState.currentAction.destination.x == world.flag.x and player.playerState.currentAction.destination.y == world.flag.y)):
                    # if the player is not going to the flag, send him to the flag
                    a.append(actions.MoveAction(player, world.flag.point))
            else:
                # if the holder is from the OTHER team, we chase him! (follow stupidly)
                if(world.flag.holder is not None and world.flag.holder.team.id != self.teamId):
                    # chase
                    a.append(actions.MoveAction(player, world.flag.holder.point))
                elif(player.isFlagHolder and player.x == world.teams[self.teamId].startingPosition.x and player.y == world.teams[self.teamId].startingPosition.y):
                    # if the player is holding the flag and is on the starting position, we drop the flag to win the game
                    a.append(actions.DropFlagAction(player))
                elif(player.isFlagHolder and player.playerState.stateType != world_module.StateType.Moving):
                    # if the player is the flag holder and is not moving
                    
                    # if the player is not at the starting point, we move it there
                    if(player.x != world.teams[self.teamId].startingPosition.x or player.y != world.teams[self.teamId].startingPosition.y):
                        a.append(actions.MoveAction(player, world.teams[self.teamId].startingPosition))
                else:
                    if(player.playerState.stateType == world_module.StateType.Idle and player.playerState.pendingAction is None):
                        # if the player isn't doing anything and has no pending actions, do something random!!! Throw or move!
                        p = None
                        while(True):
                            # random point
                            p = world_module.Point(random.random() * world.map.width, random.random() * world.map.height)
                            
                            # while the point is not in a wall and far enough from the player
                            if((not world.map.isPointInWall(p)) and utilities.maths.getEuclidianDistance(player.x, player.y, p.x, p.y) > 50):
                                break
                        
                        if(random.random() * 2 < 0.5):
                            a.append(actions.MoveAction(player, p))
                        else:
                            a.append(actions.ThrowAction(player, p))
            
            # examples of helpers you can use!!! read the doc!
            for otherPlayer in world.otherTeams[0].players:
                player.canHit(otherPlayer)
                player.canBeHitBy(otherPlayer)
                player.wouldHitPlayer(otherPlayer, world_module.Point(500.0, 500.0))
                player.canSee(otherPlayer)
                
            for snowball in world.snowballs:
                snowball.canHit(player)
                
            line1 = utilities.maths.getLine(player.point, world.flag.point)
            line2 = utilities.maths.getLine(world_module.Point(0.0, 0.0), world_module.Point(1000.0, 1000.0))
            line1.intersect(line2)
            line1.getX(0.0)
            line1.getX(0.5)
            line1.getX(1.0)
            
            world.map.isCrossingWall(world_module.Point(0.0, 0.0), world_module.Point(1000.0, 1000.0))
            world.map.isPointInWall(50.0, 50.0)
            # end of the examples        
        
        return a
