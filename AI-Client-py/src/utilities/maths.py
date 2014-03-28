'''
Contains various math helpers. 

@author: Mathieu Plourde - mat.plourde@gmail.com
'''

import math
import world

def getEuclidianDistance(*args):
    '''
    2 args (p0 [Point], p1 [Point]): Returns the Euclidian distance between the 2 Point objects p0 and p1.
    4 args (x0 [float], y0 [float], x1 [float], y1 [float]): Returns the Euclidian distance between the 2 points (x0, y0) and (x1, y1).
    
    :returns: float variable
    '''
    if len(args) == 4:
        x0 = args[0]
        y0 = args[1]
        x1 = args[2]
        y1 = args[3]
        return math.sqrt(math.pow(x1 - x0, 2) + math.pow(y1 - y0, 2))
    else:
        p0 = args[0]
        p1 = args[1]
        return math.sqrt(math.pow(p1.x - p0.x, 2) + math.pow(p1.y - p0.y, 2))
    
def getDotProduct(ux, uy, vx, vy):
    '''
    Returns the dot product of vectors u(x,y) and v(x,y).
    
    :param ux: float variable
    :param uy: float variable
    :param vx: float variable
    :param vy: float variable
    :returns: float variable
    '''
    return ux*vx + uy*vy

class ParametricLine(object):
    '''
    Represents a parametric line between two points.
    
    A parametric line is represented by 2 points: a source (x0, y0) and a destination (x1, y1).
    It represents x and y as functions of t:
    x(t) = (x1 - x0)*t + x0
    y(t) = (y1 - y0)*t + y0

    t = 0 will result in (x0, y0), while t=1 will result in (x1, y1).
    '''
    def __init__(self, x0, y0, x1, y1):
        '''
        Creates a parametric line from p(x0, y0) to p(x1, y1).
        
        :param x0: float variable
        :param y0: float variable 
        :param x1: float variable
        :param y1: float variable
        '''
        self.__x0 = x0;
        self.__y0 = y0;
        self.__x1 = x1;
        self.__y1 = y1;
        
        self.__p0 = world.Point(x0, y0)
        self.__p1 = world.Point(x1, y1)
        
        self.__mx = x1 - x0
        self.__bx = x0
        
        self.__my = y1 - y0
        self.__by = y0
    
    def getX(self, t):
        '''
        Returns X from the specified t value.
        
        :param t: float variable
        :returns: float variable
        '''
        return self.__mx * t + self.__bx
    
    def getY(self, t):
        '''
        Returns Y from the specified t value.
        
        :param t: float variable
        :returns: float variable
        '''
        return self.__my * t + self.__by
    
    def isXInBounds(self, x):
        '''
        Returns whether the specified x is within the bounds of the parametric line (t >= 0 and t <= 1).
        
        :param x: float variable
        :returns: bool variable
        '''
        return x >= min(self.__x0, self.__x1) and x <= max(self.__x0, self.__x1)
    
    def isYInBounds(self, y):
        '''
        Returns whether the specified y is within the bounds of the parametric line (t >= 0 and t <= 1).
        
        :param y: float variable
        :returns: bool variable
        '''
        return y >= min(self.__y0, self.__y1) and y <= max(self.__y0, self.__y1)
    
    def __getMx(self):
        '''
        Returns the mx factor (x1 - x0).
        
        :returns: float variable
        '''
        return self.__mx
    
    def __getMy(self):
        '''
        Returns the my factor (y1 - y0)
        
        :returns: float variable
        '''
        return self.__my
    
    def __getBx(self):
        '''
        Returns the bx factor (x0)
        
        :returns: float variable
        '''
        return self.__bx
    
    def __getBy(self):
        '''
        Returns the by factor (y0)
        
        :returns: float variable
        '''
        return self.__by
    
    def __getP0(self):
        '''
        Returns the source point (x0, y0).
        
        :returns: Point object
        '''
        return self.__p0
    
    def __getP1(self):
        '''
        Returns the destination point (x1, y1).
        
        :returns: Point object
        '''
        return self.__p1
    
    def __getX0(self):
        '''
        Returns x0.
        
        :returns: float variable
        '''
        return self.__x0
    
    def __getY0(self):
        '''
        Returns y0.
        
        :returns: float variable
        '''
        return self.__y0
    
    def __getX1(self):
        '''
        Returns x1.
        
        :returns: float variable
        '''
        return self.__x1
    
    def __getY1(self):
        '''
        Returns y1.
        
        :returns: float variable
        '''
        return self.__y1
    
    def intersect(self, line):
        '''
        Returns the intersection point of the current line and the specified line. Returns None if they don't intersect.
        
        :param line: ParametricLine object
        :returns: Point object
        '''
        denom = float(self.__mx * line.my - line.mx * self.__my)
        if denom != 0:
            t = float(self.__mx*(self.__y0-line.y0) - (self.__x0-line.x0)*self.__my) / denom
            return world.Point(line.mx*t + line.bx, line.my*t + line.by)
        return None
    
    mx = property(__getMx)
    my = property(__getMy)
    bx = property(__getBx)
    by = property(__getBy)
    p0 = property(__getP0)
    p1 = property(__getP1)
    x0 = property(__getX0)
    y0 = property(__getY0)
    x1 = property(__getX1)
    y1 = property(__getY1)

def getLine(*args):
    '''
    2 args (p0 [Point], p1 [Point]): Returns the Parametric line from Point objects p0 to p1.
    4 args (x0 [float], y0 [float], x1 [float], y1 [float]): Returns the Parametric line from point p(x0, y0) to p(x1, y1).

    :returns: ParametricLine object
    '''
    if len(args) == 2:
        return getLine(args[0].x, args[0].y, args[1].x, args[1].y)
    else:
        return ParametricLine(args[0], args[1], args[2], args[3])