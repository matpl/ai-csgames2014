'''
@author: Mathieu Plourde - mat.plourde@gmail.com
'''
class Action(object):
    '''
    Abstract class that represents an action that can be executed by a player.
    '''
    
    def __init__(self, player):
        self.__player = player
    
    def __getPlayer(self):
        '''
        Returns the player that executes this action.
    
        :returns: Player object
        '''
        return self.__player
    
    player = property(__getPlayer)
    
class DropFlagAction(Action):
    '''
    Action that makes the player drop the flag at its current position.
    '''
    def __init__(self, player):
        '''
        Action to drop the flag at the current player position
        
        :param player: Player object
        '''
        super(DropFlagAction, self).__init__(player)
        
    def toString(self):
        return "d " + str(self.player.team.id) + " " + str(self.player.id)
    
class IdleAction(Action):
    '''
    Action that cancels the player's current action.
    '''
    def __init__(self, player):
        '''
        Action to stop anything the player was doing (besides being dead)
        
        :param player: Player object
        '''
        super(IdleAction, self).__init__(player)
        
    def toString(self):
        return "i " + str(self.player.team.id) + " " + str(self.player.id)
    
class MoveAction(Action):
    '''
    Action that moves the player to the desired destination. If the player can't reach the destination in a straight line, the shortest path will be used by the server. If the destination is in a wall, the action won't be executed.
    '''
    def __init__(self, player, destination):
        '''
        Action that moves the player to the desired destination. If the player can't reach the destination in a straight line, the shortest path will be used by the server. If the destination is in a wall, the action won't be executed.
        
        :param destination: Point object
        '''
        super(MoveAction, self).__init__(player)
        self.__destination = destination
        
    def __getDestination(self):
        '''
        Returns the expected destination of the player.
        
        :returns: Point object
        '''
        return self.__destination
    
    def __setDestination(self, destination):
        self.__destination = destination
        
    def toString(self):
        return "m " + str(self.player.team.id) + " " + str(self.player.id) + " " + str(self.__destination.x) + " " + str(self.__destination.y)
        
    destination = property(__getDestination, __setDestination)
    
class PickFlagAction(Action):
    '''
    Action that makes the player pick the flag if he can do it. To be able to pick the flag, the proportion of dead players has to be >= DEAD_PROPORTION_TO_PICK and the distance to the flag has to be <= MIN_DISTANCE_TO_PICK.
    '''
    MIN_DISTANCE_TO_PICK = 10.0
    '''
    Minimum distance to pick the flag.
    '''
    DEAD_PROPORTION_TO_PICK = 0.2
    '''
    Dead players proportion to pick the flag.
    '''
    
    def __init__(self, player):
        '''
        Action that makes the player pick the flag if he can do it. To be able to pick the flag, the proportion of dead players has to be >= DEAD_PROPORTION_TO_PICK and the distance to the flag has to be <= MIN_DISTANCE_TO_PICK.
        
        :param player: Player object
        '''
        super(PickFlagAction, self).__init__(player)
        
    def toString(self):
        return "p " + str(self.player.team.id) + " " + str(self.player.id)
    
class ThrowAction(Action):
    '''
    Actions that makes the player throw a snowball at the desired destination. The player will charge his shot for a certain amount of time before throwing. The number of charging frames is defined by (distance / 15).

    The minimum/maximum distances the player can throw are determined by MIN_DISTANCE and MAX_DISTANCE. If the destination is beyond one of those values, it will be rounded up/down to MIN_DISTANCE / MAX_DISTANCE.
    
    The speed of the snowball depends of the throwing distance. At DISTANCE_FOR_MIN_SPEED, the speed is MIN_SPEED. Any distance below that will have the same speed. At DISTANCE_FOR_MAX_SPEED, the speed is MAX_SPEED. Any distance higher will have the same speed.
    
    The damage of the snowball = distance * DAMAGE_PER_DISTANCE_UNIT
    
    A snowball will hit any player in a radius of MIN_HIT_DISTANCE, and it disappears on impact. If many players are within MIN_HIT_DISTANCE, they will all be damaged.
    '''
    MIN_HIT_DISTANCE = 15.0
    '''
    Minimum distance from the snowball to be hit.
    '''
    MIN_DISTANCE = 50.0
    '''
    Minimum throw distance.
    '''
    MAX_DISTANCE = 600.0
    '''
    Maximum throw distance.
    '''
    DAMAGE_PER_DISTANCE_UNIT = 0.1
    '''
    Snowball damage per distance unit travelled.
    '''
    DISTANCE_FOR_MAX_SPEED = 400.0
    '''
    Throw distance to reach the maximum snowball speed.
    '''
    DISTANCE_FOR_MIN_SPEED = 100.0
    '''
    Throw distance to reach the minimum snowball speed.
    '''
    MIN_SPEED = 5.0
    '''
    Minimum snowball speed (distance per frame).
    '''
    MAX_SPEED = 20.0
    '''
    Maximum snowball speed (distance per frame).
    '''
    
    def __init__(self, player, destination):
        '''
        Action for throwing a snowball at the destination (Refer to class comments for details).
        
        :param player: Player object
        :param destination: Point object
        '''
        super(ThrowAction, self).__init__(player)
        self.__destination = destination
        
    def __getDestination(self):
        '''
        Returns the expected snowball destination.
        
        :returns: Point object
        '''
        return self.__destination
    
    def __setDestination(self, destination):
        self.__destination = destination
        
    def __setRemainingFrames(self, frames):
        self.__remainingFrames = frames;
        
    def __getRemainingFrames(self):
        '''
        Returns the remaining charging frames of the throw action.
        
        :returns: int variable
        '''
        return self.__remainingFrames
    
    def toString(self):
        return "t " + str(self.player.team.id) + " " + str(self.player.id) + " " + str(self.__destination.x) + " " + str(self.__destination.y)
        
    destination = property(__getDestination, __setDestination)
    remainingFrames = property(__getRemainingFrames)
    