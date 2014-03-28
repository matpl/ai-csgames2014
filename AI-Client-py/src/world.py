'''
@author: Mathieu Plourde - mat.plourde@gmail.com
'''

import socket
import sys
from abc import ABCMeta, abstractmethod
from math import pow
from utilities import maths
import actions

import struct

class Gang(object):
    '''
    Represents the game client. To provide an implementation, override the compute method, which is called every 33 ms (30 FPS).
    '''
    HOST = "127.0.0.1"
    
    '''
    classdocs
    '''
    @abstractmethod
    def compute(self, world):
        '''
        The one method that rules them all. It is called every 33 ms (30 FPS). Implement this and return a list of actions.
        Only one action per player of your own team is permitted. Sending an action for a player will cancel its current one.
        If you take more than 33 ms to respond, your AI will simply skip a frame.
        Warning: The actions you send won't necessarily be parsed in the next frame, so make sure to only send an action if the player's playerState.pendingAction is None.
        
        :param world: World object
        '''

    def __init__(self, port, name):
        self.__port = port;
        print "Trying to connect to host..."
        self.__sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.__sock.connect((self.HOST, self.__port));
        self.__makefile = self.__sock.makefile('rw')
        print "Connected!"
        worldStr = self.__makefile.readline().split(' ')
        
        self.__teamId = int(self.__makefile.readline())
        
        self.__sock.send(name.strip() + '\n')
        
        self.__snowballs = []
        
        width = float(worldStr[0])
        height = float(worldStr[1])
        
        wallCount = int(worldStr[2])
        walls = []
        
        currentPosition = 3
        
        for i in range(wallCount):
            verticesCount = int(worldStr[currentPosition]) 
            vertices = []
            currentPosition = currentPosition + 1
            for j in range(verticesCount):
                vertex = worldStr[currentPosition].split(',')
                vertices.append(Point(float(vertex[0]), float(vertex[1])))
                currentPosition = currentPosition + 1
            walls.append(Wall(vertices))
            
        map = Map(width, height, walls)
        
        teams = []
        teamCount = int(worldStr[currentPosition])
        
        for i in range(teamCount):
            currentPosition = currentPosition + 1
            players = []
            playerCount = int(worldStr[currentPosition])
            
            currentPosition = currentPosition + 1
            startingPositionStr = worldStr[currentPosition].split(',');
            startingPosition = Point(float(startingPositionStr[0]), float(startingPositionStr[1]))
            
            for j in range(playerCount):
                currentPosition = currentPosition + 1
                
                players.append(Player(int(worldStr[currentPosition])))
                players[j]._Entity__setX(startingPosition.x)
                players[j]._Entity__setY(startingPosition.y)
                
            teams.append(Team(i, startingPosition, players))
            
            for player in players:
                player._Player__setTeam(teams[i])
            
        currentPosition = currentPosition + 1
        flagX = float(worldStr[currentPosition])
        currentPosition = currentPosition + 1
        flagY = float(worldStr[currentPosition])
        
        flag = Flag()
        flag._Entity__setX(flagX)
        flag._Entity__setY(flagY)
        
        self.__world = World(teams, map, flag, []);
        
        flag._Entity__setWorld(self.__world)
        for t in self.__world.teams:
            for p in t.players:
                p._Entity__setWorld(self.__world)
        
        self.__world._World__setCurrentTeamId(self.__teamId)
        
        self.__containsPlayerAction = []
        for i in range(len(self.__world.teams[self.__teamId].players)):
            self.__containsPlayerAction.append(False)
        
    def loop(self):
        visibilitiesCount = (len(self.__world.teams) - 1) * len(self.__world.teams[0].players)
        
        
        while(True):
            count = 0
            for i in range(4):
                count = ord(self.__sock.recv(1)) << (i*8) | count
            
            updates = self.__sock.recv(count)
            
            visibilities = self.__sock.recv(visibilitiesCount)
            visibleSnowballs = None
            
            snowballSize = self.__sock.recv(4)
            snowballCount = self.__bytesToInt(snowballSize, 0)
            
            if(snowballCount > 0):
                visibleSnowballs = self.__sock.recv(snowballCount * 4)
                
            self.__parseUpdates(updates)
            self.__parsePlayerVisibilities(visibilities)
            self.__parseSnowballVisibilities(visibleSnowballs)
            
            actions = self.compute(self.__world)
            
            
            
            for i in range(len(self.__containsPlayerAction)):
                self.__containsPlayerAction[i] = False
                
            for i in range(len(actions) - 1, -1, -1):
                if(actions[i].player.team.id != self.__teamId or self.__containsPlayerAction[actions[i].player.id % len(self.__world.teams[self.__teamId].players)]):
                    del actions[i]
                else:
                    self.__containsPlayerAction[actions[i].player.id % len(self.__world.teams[self.__teamId].players)] = True
                
            cmd = ''
            for i in range(len(actions)):
                if(i != 0):
                    cmd = cmd + ','
                    
                actions[i].player.playerState._PlayerState__setPendingAction(actions[i])
                actions[i].player.playerState._PlayerState__setFrameCount(self.__world.currentTeam._Team__getFrameCount() + 1)
                    
                cmd = cmd + actions[i].toString()
            
            cmd = cmd + '\n'
            
            if(len(actions) != 0):
                self.__world.currentTeam._Team__setFrameCount(self.__world.currentTeam._Team__getFrameCount() + 1)
            
            self.__makefile.write(cmd)
            self.__makefile.flush()
        
    def __parseUpdates(self, updates):
        self.__world._World__setCurrentFrame(self.__bytesToInt(updates, 0))
        current = 3
        frameCount = 0
        for i in range(len(self.__world.teams)):
            
            current = current + 1
            frameCount = self.__bytesToInt(updates, current)
            current = current + 3
            
            alivePlayersCount = 0;
            
            for j in range(len(self.__world.teams[i].players)):
                current = current + 1
                p = self.__world.teams[i].players[j]
                p._Entity__setVisible(True)
                
                # health
                current = current + 1
                p._Player__setHealth(self.__bytesToDouble(updates, current))
                
                # position
                current = current + 8
                p.point.x = self.__bytesToDouble(updates, current)
                
                current = current + 8
                p.point.y = self.__bytesToDouble(updates, current)
                
                # orientation
                current = current + 8
                p.orientation.x = self.__bytesToDouble(updates, current)
                current = current + 8
                p.orientation.y = self.__bytesToDouble(updates, current)
                
                # flag
                current = current + 8
                if(ord(updates[current]) == 0):
                    p._Player__setFlagHolder(False)
                else:
                    p._Player__setFlagHolder(True)
                    self.__world.flag._Flag__setHolder(p)
                    
                # state
                current = current + 1
                
                if(ord(updates[current]) == 1):
                    # idle
                    p.playerState._PlayerState__setCurrentAction(None)
                    p.playerState._PlayerState__setStateType(StateType.Idle)
                    
                    alivePlayersCount = alivePlayersCount + 1
                elif(ord(updates[current]) == 2):
                    # moving
                    p.playerState._PlayerState__setStateType(StateType.Moving)
                    current = current + 1
                    if(p.team.id == self.__teamId):
                        if(p.playerState.currentAction is None or (not isinstance(p.playerState.currentAction, actions.MoveAction))):
                            p.playerState._PlayerState__setCurrentAction(actions.MoveAction(p, Point(self.__bytesToDouble(updates, current), self.__bytesToDouble(updates, current + 8))))
                    else:
                        p.playerState._PlayerState__setCurrentAction(None)
                        
                    current = current + 15
                    
                    alivePlayersCount = alivePlayersCount + 1
                elif(ord(updates[current]) == 3):
                    # throwing
                    p.playerState._PlayerState__setStateType(StateType.Throwing)
                    current = current + 1
                    if(p.team.id == self.__teamId):
                        if(p.playerState.currentAction is None or (not isinstance(p.playerState.currentAction, actions.ThrowAction))):
                            p.playerState._PlayerState__setCurrentAction(actions.ThrowAction(p, Point(self.__bytesToDouble(updates, current), self.__bytesToDouble(updates, current + 8))))
                        p.playerState.currentAction._ThrowAction__setRemainingFrames(self.__bytesToInt(updates, current + 16))
                    else:
                        p.playerState._PlayerState__setCurrentAction(None)
                        
                    current = current + 19
                    
                    alivePlayersCount = alivePlayersCount + 1
                elif(ord(updates[current]) == 4):
                    # dead
                    p.playerState._PlayerState__setCurrentAction(None)
                    p.playerState._PlayerState__setStateType(StateType.Dead)
                    
                if(i == self.__teamId and p.playerState.pendingAction is not None and frameCount >= p.playerState._PlayerState__getFrameCount()):
                    p.playerState._PlayerState__setPendingAction(None);
                
            self.__world.teams[i]._Team__setAlivePlayersCount(alivePlayersCount)
        
        # flag stuff
        current = current + 1
        self.__world.flag._Entity__setX(self.__bytesToDouble(updates, current))        
        current = current + 8
        self.__world.flag._Entity__setY(self.__bytesToDouble(updates, current))
        current = current + 8
        
        if(ord(updates[current]) == 0):
            self.__world._World__setCanPickFlag(False)
        else:
            self.__world._World__setCanPickFlag(True)
            
        current = current + 1
        if(ord(updates[current]) == 0):
            self.__world.flag._Flag__setHolder(None)
            
        current = current + 1
        snowballCount = self.__bytesToInt(updates, current)
        current = current + 3
        
        for i in range(len(self.__snowballs)):
            # little hack... we use visible instead of creating another list...
            self.__snowballs[i]._Entity__setVisible(False)
        
        for i in range(snowballCount):
            current = current + 1
            id = self.__bytesToInt(updates, current)
            s = None
            for j in range(len(self.__snowballs)):
                if(self.__snowballs[j].id == id):
                    s = self.__snowballs[j]
                    break
            
            if(s is None):
                s = Snowball(self.__world, id)
                self.__snowballs.append(s)
            
            s._Entity__setVisible(True)
            
            current = current + 4
            s._Entity__setX(self.__bytesToDouble(updates, current))
            current = current + 8
            s._Entity__setY(self.__bytesToDouble(updates, current))
            current = current + 8
            s.orientation.x = self.__bytesToDouble(updates, current)
            current = current + 8
            s.orientation.y = self.__bytesToDouble(updates, current)
            current = current + 8
            s.destination.x = self.__bytesToDouble(updates, current)
            current = current + 8
            s.destination.y = self.__bytesToDouble(updates, current)
            current = current + 8
            s._Snowball__setDamage(self.__bytesToDouble(updates, current))
            current = current + 8
            s._Snowball__setSpeed(self.__bytesToDouble(updates, current))
            current = current + 7
        
        for i in range(len(self.__snowballs) - 1, -1, -1):
            if(not self.__snowballs[i].isVisible):
                del self.__snowballs[i]
    
    def __parsePlayerVisibilities(self, visibilities):
        pos = 0
        
        for i in range(len(self.__world.teams)):
            if(i != self.__teamId):
                t = self.__world.teams[i]
                for j in range(len(t.players)):
                    if(ord(visibilities[pos]) == 0 and (not t.players[j].isFlagHolder)):
                        t.players[j]._Entity__setVisible(False)
                        
                    pos = pos + 1
                    
    def __parseSnowballVisibilities(self, visibilities):
        for i in range(len(self.__snowballs)):
            self.__snowballs[i]._Entity__setVisible(False)
            
        if(visibilities is not None):
            for i in range(0, len(visibilities), 4):
                no = self.__bytesToInt(visibilities, i)
                for j in range(len(self.__snowballs)):
                    if(no == self.__snowballs[j].id):
                        self.__snowballs[j]._Entity__setVisible(True)
                        break;
                    
        del self.__world.snowballs[:]
        
        for i in range(len(self.__snowballs)):
            if(self.__snowballs[i].isVisible):
                self.__world.snowballs.append(self.__snowballs[i])
    
    def __bytesToDouble(self, array, position):
        l = 0
        for i in range(8):
            l = l + (long(ord(array[position+i]) & 0xFFL) << (i << 3))
            
        d = self.__longBitsToDouble(l)
        
        return d
    
    def __bytesToInt(self, array, position):
        v = 0
        for i in range(4):
            v = v + (int(ord(array[position+i]) & 0xFF) << (i << 3))
            
        return v
    
    def __longBitsToDouble(self, bits):
        """
        @type  bits: long
        @param bits: the bit pattern in IEEE 754 layout
    
        @rtype:  float
        @return: the double-precision floating-point value corresponding
                 to the given bit pattern C{bits}.
        """
        return struct.unpack('d', struct.pack('Q', bits))[0]
    
    def __getTeamId(self):
        return self.__teamId
    
    teamId = property(__getTeamId)
        
class Point(object):
    '''
    Point containing x and y coordinates.
    '''
    def __init__(self, x, y):
        '''
        Creates a new point with the specified x and y coordinates.
    
        :param x: float variable
        :param y: float variable
        '''
        self.__x = x
        self.__y = y
        
    def __getX(self):
        '''
        Returns X
        
        :returns: float variable
        '''
        return self.__x
    
    def __getY(self):
        '''
        Returns Y
        
        :returns: float variable
        '''
        return self.__y
    
    def __setX(self, value):
        '''
        Sets X
        
        :param value: float variable
        '''
        self.__x = value
        
    def __setY(self, value):
        '''
        Sets Y
        
        :param value: float variable
        '''
        self.__y = value
        
    x = property(__getX, __setX)
    y = property(__getY, __setY)
    
class Wall(object):
    
    def __init__(self, vertices):
        self.__vertices = vertices
        self.__x1ParamList = []
        self.__x2Mx1ParamList = []
        self.__y1ParamList = []
        self.__y2My1ParamList = []
        
        self.__minX = sys.float_info.max
        self.__minY = sys.float_info.max
        self.__maxX = 0
        self.__maxY = 0
        
        for i in range(len(vertices)):
            if(vertices[i].x < self.__minX):
                self.__minX = vertices[i].x
            if(vertices[i].y < self.__minY):
                self.__minY = vertices[i].y
            if(vertices[i].x > self.__maxX):
                self.__maxX = vertices[i].x
            if(vertices[i].y > self.__maxY):
                self.__maxY = vertices[i].y
            
            p1 = Point(vertices[i].x, vertices[i].y)
            p2 = Point(vertices[(i+1)%len(vertices)].x, vertices[(i+1)%len(vertices)].y)
            if(p1.x > p2.x):
                temp = p2.x
                p2.x = p1.x
                p1.x = temp
                temp = p2.y
                p2.y = p1.y
                p1.y = temp
            
            self.__x1ParamList.append(p1.x)
            self.__x2Mx1ParamList.append(p2.x - p1.x)
            self.__y1ParamList.append(p1.y)
            self.__y2My1ParamList.append(p2.y - p1.y)
        
    def isPointInWall(self, *args):
        '''
        1 arg (Point): Returns whether the point is inside the wall or not. The point has to be inside the wall and not on the border.
        2 args (x [float], y [float]): Returns whether p(x, y) is inside the wall or not. The point has to be inside the wall and not on the border.
        
        returns a bool
        '''
        if(len(args) == 1):
            return self.isPointInWall(args[0].x, args[0].y)
        else:
            x = args[0]
            y = args[1]
            if(x <= self.__minX or x >= self.__maxX or y <= self.__minY or y >= self.__maxY):
                return False
            
            c = False
            j = len(self.__vertices) - 1
            for i in range(len(self.__vertices)):
                if ( ((self.__vertices[i].y>y) != (self.__vertices[j].y>y)) and (x < (self.__vertices[j].x-self.__vertices[i].x) * (y-self.__vertices[i].y) / (self.__vertices[j].y-self.__vertices[i].y) + self.__vertices[i].x) ):
                    c = not c
                j = i
                
            if c:
                for i in range(len(self.__x1ParamList)):
                    if(self.__x2Mx1ParamList[i] == 0):
                        t = (y - self.__y1ParamList[i]) / self.__y2My1ParamList[i]
                        if(x == self.__x1ParamList[i] and t >= 0 and t <= 1):
                            return False
                    elif(self.__y2My1ParamList[i] == 0):
                        t = (x - self.__x1ParamList[i]) / self.__x2Mx1ParamList[i]
                        if(y == self.__y1ParamList[i] and t >= 0 and t <= 1):
                            return False
                    else:
                        # compute 2 t
                        t = int(((x - self.__x1ParamList[i]) / self.__x2Mx1ParamList[i] * 10000.0))/10000.0
                        if(t == int(((y - self.__y1ParamList[i]) / self.__y2My1ParamList[i] * 10000.0))/10000.0):
                            return False
                        
            return c
        
    def __isLineInWall(self, x1, y1, x2Mx1, y2My1):
        queue = []
        
        for i in range(len(self.__vertices)):
            # parametric nightmare
            
            denom = (self.__x2Mx1ParamList[i]*y2My1 - x2Mx1*self.__y2My1ParamList[i])
            
            if(denom != 0): 
                t = ((self.__x2Mx1ParamList[i])*(self.__y1ParamList[i]-y1) - (self.__x1ParamList[i]-x1)*self.__y2My1ParamList[i])/denom;
            
                if(t >= 0 and t <= 1):
                    queue.append(t)
        
        queue = sorted(queue)
        
        if(len(queue) > 0):
            lastT = queue[0]
            
            for i in range(1, len(queue)):
                middleT = (t + lastT) / 2.0
                
                xt = x2Mx1 * middleT + x1
                yt = y2My1 * middleT + y1
                
                if(self.isPointInWall(xt, yt)):
                    return True
                
                lastT = t;
            
        return False
    
    def __getVertices(self):
        '''
        Returns the list of vertices in clockwise order.
        
        :returns: list of Point objects
        '''
        return self.__vertices
    
    vertices = property(__getVertices)
        
        
class Map(object):
    '''
    Map of the game. Contains an array of walls and some helpers.
    '''    
    def __init__(self, width, height, walls):
        self.__width = width
        self.__height = height
        self.__walls = walls
        
    def __getWidth(self):
        '''
        Returns the width of the map.
        
        :returns: float variable
        '''
        return self.__width
    
    def __getHeight(self):
        '''
        Returns the height of the map.
        
        :returns: float variable
        '''
        return self.__height
    
    def __getWalls(self):
        '''
        Returns the list of walls.
        
        :returns: list of Wall objects
        '''
        return self.__walls
        
    width = property(__getWidth)
    height = property(__getHeight)
    walls = property(__getWalls)
    
    def isPointInWall(self, *args):
        '''
        1 arg (Point): Returns whether the point is in a wall or not. To be in a wall, the point has to be inside the wall and not on the border.
        2 args (x [float], y [float]): Returns whether p(x, y) is in a wall or not. To be in a wall, the point has to be inside the wall and not on the border.
        
        :returns: bool variable
        '''
        if(len(args) == 1):
            return self.isPointInWall(args[0].x, args[0].y)
        else:
            x = args[0]
            y = args[1]
            for wall in self.__walls:
                if(wall.isPointInWall(x, y)):
                    return True
            return False
    
    def isCrossingWall(self, *args):
        '''
        2 args (source [Point], destination [Point]): Returns whether the line (source, destination) is crossing a wall or not. Touching a border doesn't count as crossing a wall.
        4 args (x0 [float], y0 [float], x1 [float], y1 [float]): Returns whether the line (p(x0,y0), p(x1, y1)) is crossing a wall or not. Touching a border doesn't count as crossing a wall.
        
        :returns: bool variable
        '''
        if(len(args) == 2):
            return self.isCrossingWall(args[0].x, args[0].y, args[1].x, args[1].y)
        else:
            x0 = args[0]
            y0 = args[1]
            x1 = args[2]
            y1 = args[3]
            
            if(self.isPointInWall(x0, y0) or self.isPointInWall(x1, y1)):
                return True
            
            x1Mx0 = x1 - x0
            y1My0 = y1 - y0
            
            for wall in self.__walls:
                if(wall._Wall__isLineInWall(x0, y0, x1Mx0, y1My0)):
                    return True
            
            return False
    
class StateType(object):
    '''
    Different state types for player. The state type depends of what the player is currently doing.
    '''
    Idle = 0
    '''
    Player isn't doing anything. Probably eating snow or something.
    '''
    Moving = 1
    '''
    Player is moving.
    '''
    Throwing = 2
    '''
    Player is throwing a snowball (charging his shot).
    '''
    Dead = 3
    '''
    Player is dead.
    '''
    Unknown = 4
    '''
    Player isn't visible to the current team.
    '''
        
class PlayerState(object):
    '''
    Represents the current state of the player. Contains its current action, state type, pending action.
    '''
    def __init__(self, stateType, currentAction):
        self.__stateType = stateType
        self.__currentAction = currentAction
        self.__visible = False
        self.__pendingAction = None
        self.__frameCount = 0
        
    def __getCurrentAction(self):
        '''
        Returns the current action being executed by the player. The current action is ALWAYS None if the player doesn't belong to the current team.
        
        :returns: Action object
        '''
        if(not self.__visible):
            return None
        return self.__currentAction
    
    def __getStateType(self):
        '''
        Returns the type of state the player is in. Refer to the StateType class for details. Returns StateType.Unknown if the player isn't currently visible.
        
        :returns: int variable        
        '''
        if(not self.__visible):
            return StateType.Unknown
        return self.__stateType
    
    def __setCurrentAction(self, currentAction):
        self.__currentAction = currentAction
    
    def __setStateType(self, stateType):
        self.__stateType = stateType
        
    def __isVisible(self):
        return self.__visible
    
    def __setVisible(self, visible):
        self.__visible = visible
        
    def __setPendingAction(self, pendingAction):
        self.__pendingAction = pendingAction
        
    def __getPendingAction(self):
        '''
        Returns the pending action for the player. A pending action is an action sent to the server that hasn't been processed yet. The pending action is ALWAYS None if the player doesn't belong to the current team.
        
        :returns: Action object
        '''
        return self.__pendingAction
    
    def __setFrameCount(self, frameCount):
        self.__frameCount = frameCount
        
    def __getFrameCount(self):
        return self.__frameCount
        
    currentAction = property(__getCurrentAction)
    stateType = property(__getStateType)
    pendingAction = property(__getPendingAction)
    
class Team(object):
    '''
    Contains everything related to a team, including the players, the starting position, the number of alive players, etc.
    '''
    def __init__(self, teamId, startingPosition, players):
        self.__players = players
        self.__startingPosition = startingPosition
        self.__id = teamId
        self.__frameCount = 0
        self.__alivePlayersCount = len(players)
        
    def __getId(self):
        '''
        Returns the id of the team.
        
        :returns: int variable
        '''
        return self.__id
    
    def __getPlayers(self):
        '''
        Returns a list of the players contained in the team
        
        :returns: list of Player objects
        '''
        return self.__players
    
    def __getStartingPosition(self):
        '''
        Returns the starting position of the team in the map
        
        :returns: Point object
        '''
        return self.__startingPosition
    
    def __getFrameCount(self):
        return self.__frameCount
    
    def __setFrameCount(self, frameCount):
        self.__frameCount = frameCount
    
    def __getAlivePlayersCount(self):
        '''
        Returns the number of alive players in the team.
        
        :returns: int variable
        '''
        return self.__alivePlayersCount
    
    def __setAlivePlayersCount(self, alivePlayersCount):
        self.__alivePlayersCount = alivePlayersCount
    
    def getPlayerById(self, playerId):
        '''
        Returns a player by its id (None if the id is not valid).
        
        :param playerId: int variable
        :returns: Player object
        '''
        for player in self.__players:
            if(player.id == playerId):
                return player
        return None
    
    def getFlagHolder(self):
        '''
        Returns the flag holder (None if no one holds the flag)
        
        :returns: Player object
        '''
        for player in self.__players:
            if(player.isFlagHolder):
                return player
        return None
    
    id = property(__getId)
    players = property(__getPlayers)
    startingPosition = property(__getStartingPosition)
    alivePlayersCount = property(__getAlivePlayersCount)
        
class Entity(object):
    '''
    Abstract Entity class for objects in a world.
    '''
    def __init__(self):
        self.__p = Point(-1, -1)
        self.__orientation = Point(-1,-1)
        self.__world = None
        self.__visible = False
    
    def __setWorld(self, world):
        self.__world = world
    
    def __getX(self):
        '''
        Returns the X coordinate of the entity.
        
        :returns: float variable
        '''
        if(not self.__visible):
            return -1
        return self.__p.x
    
    def __getY(self):
        '''
        Returns the Y coordinate of the entity.
        
        :returns: float variable
        '''
        if(not self.__visible):
            return -1
        return self.__p.y
    
    def __setX(self, x):
        self.__p.x = x
        
    def __setY(self, y):
        self.__p.y = y
        
    def __getPoint(self):
        '''
        Returns the position of the entity.
        
        :returns: Point object
        '''
        if(not self.__visible):
            return None
        return self.__p
    
    def __isVisible(self):
        '''
        Returns whether the entity is visible or not.
        
        :returns: bool variable
        '''
        return self.__visible
    
    def __setVisible(self, visible):
        self.__visible = visible
        
    def __canHit(self, x, y, startX, startY, destX, destY, orientation):
        if(not self._Entity__isVisible):
            return False
        
        if orientation is None:
            orientation = self.__orientation
        
        canHit = False
        
        ux = x - startX
        uy = y - startY
        
        dotP = maths.getDotProduct(orientation.x, orientation.y, ux, uy) / pow(maths.getEuclidianDistance(0, 0, orientation.x, orientation.y), 2)
        
        if dotP >= 0:        
            px = startX + dotP*orientation.x
            py = startY + dotP*orientation.y
            
            #px py should be on the line
            if (px >= min(startX, destX) and px <= max(startX, destX) and py >= min(startY, destY) and py <= max(startY, destY) and (not self.__world.map.isCrossingWall(startX, startY, px, py)) and maths.getEuclidianDistance(px, py, x, y) <= actions.ThrowAction.MIN_HIT_DISTANCE):
                canHit = True
            elif(maths.getEuclidianDistance(destX, destY, x, y) <= actions.ThrowAction.MIN_HIT_DISTANCE and (not self.__world.map.isCrossingWall(startX, startY, destX, destY)) and (not self.__world.map.isCrossingWall(x, y, destX, destY))):
                canHit = True
           
        return canHit
        
    x = property(__getX)
    y = property(__getY)
    isVisible = property(__isVisible)
    point = property(__getPoint)
        
class Player(Entity):
    '''
    Player in a team. Provides basic getters and some helpers.
    '''
    MAX_HEALTH = 100
    '''
    Max health of a player.
    '''
    
    def __init__(self, *args):
        super(Player, self).__init__()
        self.__isFlagHolder = False
        self.__health = self.MAX_HEALTH
        self.__state = PlayerState(StateType.Idle, None)
        self.__playerId = args[0]
        if(len(args) > 1):
            self.__team = args[1]
            self.__state = args[2]
            
    def __getId(self):
        '''
        Returns the player id.
        
        :returns: int variable
        '''
        return self.__playerId
    
    def _Entity__setVisible(self, visible):
        self.__state._PlayerState__setVisible(visible)
        Entity._Entity__setVisible(self, visible)
        
    def __getPlayerState(self):
        '''
        Returns the player state, which includes the state type, current action, pending action. Returns None if the player isn't currently visible.
        
        :returns: PlayerState object
        '''
        if(not self.isVisible):
            return None
        return self.__state
    
    def __setPlayerState(self, state):
        self.__state = state
    
    def __getOrientation(self):
        '''
        Returns the unit vector (length of 1) of the orientation of the player. Returns None if the player isn't currently visible.
        
        :returns: Point object
        '''
        if(not self.isVisible):
            return None
        return self._Entity__orientation
    
    def __getTeam(self):
        '''
        Returns the team containing the current player.
        
        :returns: Team object
        '''
        return self.__team
    
    def __setTeam(self, team):
        self.__team = team
        
    def __getHealth(self):
        '''
        Returns the health of the player. Returns -1 if the player isn't currently visible.
        
        :returns: float variable
        '''
        if(not self.isVisible):
            return -1
        return self.__health
    
    def __setHealth(self, health):
        self.__health = health
        
    def __getIsFlagHolder(self):
        '''
        Returns whether the player is holding the flag. If the player is holding the flag, he becomes visible to ALL.
        
        :returns: bool variable
        '''
        return self.__isFlagHolder
    
    def __setFlagHolder(self, isFlagHolder):
        self.__isFlagHolder = isFlagHolder
    
    def __getSpeed(self):
        '''
        Returns the distance traveled per frame the player, which is directly related to its health. Returns -1 if the player isn't currently visible.
        distancePerFrame = 5.0 (or 2.5 if the player is the flag holder)
        speed = (distancePerFrame/2.0)*((Player.MAX_HEALTH - self.health) / Player.MAX_HEALTH)
        
        :returns: float variable
        '''
        if(not self.isVisible):
            return -1.0
        dist = 5.0
        if self.isFlagHolder:
            dist = dist / 2.0
        return dist - (dist/2.0)*((Player.MAX_HEALTH - self.health) / Player.MAX_HEALTH);
    
    def canBeHitBy(self, *args):
        '''
        args[0] is a Snowball:
        Returns whether the player can be hit by the snowball. Snowball hits if it travels close to the point (<= ThrowAction.MIN_HIT_DISTANCE).
        It doesn't check for collisions with other players along the way, but checks for collisions with walls.
        Returns false if the snowball or player isn't currently visible.
        
        args[0] is a Player:
        Returns whether the player can be hit by the specified player. The specified player has to be currently throwing.
        If the specified player is not in the current team, the throwing distance is assumed to be ThrowAction.MAX_DISTANCE.
        The specified player orientation is used to determine the snowball trajectory. Snowball hits if it travels close to the point (<= ThrowAction.MIN_HIT_DISTANCE).
        It doesn't check for collisions with other players along the way, but checks for collisions with walls.
        Returns false if one of the players isn't currently visible.
        
        :returns: bool variable
        '''
        if(isinstance(args[0], Snowball)):
            snowball = args[0]
            if (not self.isVisible) or (not snowball.isVisible):
                return False
            return snowball.canHit(self)
        elif(isinstance(args[0], Player)):
            player = args[0]
            if (not self.isVisible) or (not player.isVisible):
                return False
            if player.playerState.stateType == StateType.Throwing:
                startX = player.x
                startY = player.y 
                
                dist = maths.getEuclidianDistance(0, 0, player.orientation.x, player.orientation.y)
                endX = 0.0
                endY = 0.0
                action = player.playerState.currentAction
                if(action is not None):
                    endX = action.destination.x
                    endY = action.destination.y
                else:
                    endX = (startX + player.orientation.x - startX) * (actions.ThrowAction.MAX_DISTANCE / dist) + startX
                    endY = (startY + player.orientation.y - startY) * (actions.ThrowAction.MAX_DISTANCE / dist) + startY
                startX = (startX + player.orientation.x - startX) * (20.0 / dist) + startX
                startY = (startY + player.orientation.y - startY) * (20.0 / dist) + startY
                
                return player._Entity__canHit(self.x, self.y, startX, startY, endX, endY, None)
            
            return False
        
    def wouldHitPlayer(self, player, destination):
        '''
        Returns whether the player can hit the specified player if he shoots at the specified destination. It doesn't check for collisions with other players along the way, but checks for collisions with walls. Returns false if one of the players isn't currently visible.
        
        :param player: Player object
        :param destination: Point object
        :returns: bool variable
        '''
        if (not self.isVisible) or (not player.isVisible) or (destination.x == self.x and destination.y == self.y):
            return False
        startX = self.x
        startY = self.y
        
        dist = maths.getEuclidianDistance(destination, self.point)
        
        oriX = (destination.x - self.x) / dist
        oriY = (destination.y - self.y) / dist
        if(dist > actions.ThrowAction.MAX_DISTANCE):
            endX = (startX + oriX - startX) * (actions.ThrowAction.MAX_DISTANCE) + startX
            endY = (startY + oriY - startY) * (actions.ThrowAction.MAX_DISTANCE) + startY
        else:
            endX = destination.x
            endY = destination.y
        
        return player._Entity__canHit(player.x, player.y, startX, startY, endX, endY, Point(oriX, oriY))
    
    def canHit(self, player):
        '''
        Returns whether the player can hit the specified player. The player has to be currently throwing.
        If the player is not in the current team, the throwing distance is assumed to be ThrowAction.MAX_DISTANCE.
        The player orientation is used to determine the snowball trajectory. Snowball hits if it travels close to the point (<= ThrowAction.MIN_HIT_DISTANCE).
        It doesn't check for collisions with other players along the way, but checks for collisions with walls.
        Returns false if one of the players isn't currently visible.
        
        :param player: Player object
        :returns: bool variable
        '''
        return player.canBeHitBy(self)        
    
    def canSee(self, entity):
        '''
        Returns whether the player can see the specified entity (flag is always visible). Returns false if the player or entity isn't currently visible.
        
        :param entity: Entity object
        :returns bool variable
        '''
        if not self.isVisible:
            return False 
        if isinstance(entity, Flag):
            return True
        elif not entity.isVisible:
            return False
        
        return not self._Entity__world.map.isCrossingWall(self.point, entity.point)
    
    def isInHitRange(self, player):
        '''
        Returns whether the current player is within the specified player hit range (according to ThrowAction.MAX_DISTANCE, ThrowAction.MIN_HIT_DISTANCE, self.canSee).
        
        :param player: Player object
        :returns: bool variable
        '''
        if (not self.canSee(player)) or (not player.isVisible):
            return False
        
        return (maths.getEuclidianDistance(self.x, self.y, player.x, player.y) <= actions.ThrowAction.MAX_DISTANCE + actions.ThrowAction.MIN_HIT_DISTANCE) and self.canSee(player)
        
    id = property(__getId)
    playerState = property(__getPlayerState)
    orientation = property(__getOrientation)
    team = property(__getTeam)
    health = property(__getHealth)
    speed = property(__getSpeed)
    isFlagHolder = property(__getIsFlagHolder)

class Snowball(Entity):
    '''
    Represents an active snowball travelling in the map.
    '''
    def __init__(self, world, id):
        super(Snowball, self).__init__()
        self._Entity__setWorld(world);
        self.__id = id
        self.__destination = Point(-1, -1)
        
    def __getId(self):
        '''
        Returns the snowball id.
        
        :returns: int variable
        '''
        return self.__id
    
    def __getOrientation(self):
        '''
        Returns the unit vector (length of 1) of the orientation of the snowball. Returns None if the snowball isn't currently visible.
        
        :returns: Point object
        '''
        if(not self.isVisible):
            return None
        return self._Entity__orientation
    
    def __getSpeed(self):
        '''
        Returns the distance traveled per frame by the snowball. The ball travels faster if thrown farther (see ThrowAction for details). Returns -1 if the snowball isn't currently visible.
        
        :returns: float variable
        '''
        if(not self.isVisible):
            return -1
        return self.__speed
    
    def __setSpeed(self, speed):
        self.__speed = speed
    
    def __getDamage(self):
        '''
        Returns the damage induced to a player if the snowball hits. The damage is related to the snowball speed (see ThrowAction for details). Returns -1 if the snowball isn't currently visible.
        
        :returns: float variable
        '''
        if(not self.isVisible):
            return -1
        return self.__damage
    
    def __setDamage(self, damage):
        self.__damage = damage
    
    def __getDestination(self):
        '''
        Returns the destination where the snowball will land. Returns None if the snowball isn't currently visible.
        
        :returns: Point object
        '''
        if(not self.isVisible):
            return None
        return self.__destination
    
    def __setDestination(self, destination):
        self.__destination = destination
    
    def canHit(self, *args):
        '''
        1 arg(Player): Returns whether the snowball can the player. Snowball hits if it travels close to the players (<= ThrowAction.MIN_HIT_DISTANCE). It doesn't check for collisions with other players along the way, but checks for collisions with walls. Returns false if the snowball isn't currently visible.
        2 args(x [float], y [float]): Returns whether the snowball can hit p(x,y). Snowball hits if it travels close to the point (<= ThrowAction.MIN_HIT_DISTANCE). It doesn't check for collisions with players along the way, but checks for collisions with walls. Returns false if the snowball isn't currently visible.
        
        :returns: bool variable
        '''
        if len(args) == 2:
            x = args[0]
            y = args[1]
            return super(Snowball, self)._Entity__canHit(x, y, self.x, self.y, self.destination.x, self.destination.y, None)
        elif len(args) == 1:
            player = args[0]
            if not player.isVisible:
                return False
            return self.canHit(player.x, player.y)
    
    id = property(__getId)
    orientation = property(__getOrientation)
    destination = property(__getDestination)
    damage = property(__getDamage)
    speed = property(__getSpeed)

class Flag(Entity):
    '''
    The flag to bring back to end the game.
    '''
    def __init__(self):
        super(Flag, self).__init__()
        self._Entity__visible = True
        self.__holder = None
        
    def __getHolder(self):
        '''
        Returns the current flag holder.
        
        :returns: Player object
        '''
        return self.__holder
    
    def __setHolder(self, holder):
        self.__holder = holder
        
    holder = property(__getHolder)
    
class World(object):
    '''
    Provides everything related to the snowball fight: map, teams, snowballs, flag, etc.
    '''
    
    MAX_FRAME_COUNT = 9000
    '''
    Total number of frames in a full length game.
    9000 frames at 30 fps -> 5 minutes.
    '''
    ADVANCED_VISIBILITY_RADIUS = 225.0
    '''
    Visibility radius of players in advanced mode.
    '''
    
    def __init__(self, teams, map, flag, snowballs):
        self.__teams = teams
        self.__map = map
        self.__flag = flag
        self.__snowballs = snowballs
        self.__canPickFlag = False
        self.__currentFrame = 0
    
    def __getMap(self):
        '''
        Returns the map of the game.
        
        :returns: Map object
        '''
        return self.__map
    
    def __getTeams(self):
        '''
        Returns the list of teams.
        
        :returns: list of Team objects
        '''
        return self.__teams
    
    def __getFlag(self):
        '''
        Returns the flag of the game.
        
        :returns: Flag object
        '''
        return self.__flag
    
    def __getSnowballs(self):
        '''
        Returns a list of snowballs visible to the current team.
        
        :returns: list of Snowball objects
        '''
        return self.__snowballs
    
    def __getCanPickFlag(self):
        '''
        Returns whether the flag can/can't be picked by players.
        The flag can be picked if the proportion of dead players >= PickFlagAction.DEAD_PROPORTION_TO_PICK, or if the game is halfway done.
        
        :returns: bool variable
        '''
        return self.__canPickFlag
    
    def __setCanPickFlag(self, canPickFlag):
        self.__canPickFlag = canPickFlag
        
    def __setCurrentTeamId(self, teamId):
        self.__currentTeamId = teamId
    
    def __getOtherTeams(self):
        '''
        Returns a list containing all teams other than the current one.
        
        :returns: list of Team objects
        '''
        teams = []
        for t in self.__teams:
            if(t.id != self.__currentTeamId):
                teams.append(t)
        return teams
    
    def __getCurrentTeam(self):
        '''
        Returns the current team.
        
        :returns: Team object
        '''
        return self.__teams[self.__currentTeamId]
    
    def __getCurrentFrame(self):
        '''
        Returns the current frame of the game
        
        :returns: int variable
        '''
        return self.__currentFrame
    
    def __setCurrentFrame(self, currentFrame):
        self.__currentFrame = currentFrame
    
    map = property(__getMap)
    teams = property(__getTeams)
    flag = property(__getFlag)
    snowballs = property(__getSnowballs)
    canPickFlag = property(__getCanPickFlag)
    currentTeam = property(__getCurrentTeam)
    otherTeams = property(__getOtherTeams)
    currentFrame = property(__getCurrentFrame)
            