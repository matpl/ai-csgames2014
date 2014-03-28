'''
Created on 2014-02-09

@author: Mathieu
'''
import sys
import mygang

if __name__ == '__main__':

    port = 11114
    name = ""
    if(len(sys.argv) == 2):
        name = str(sys.argv[1])
    elif(len(sys.argv) == 3):
        name = str(sys.argv[1])    
        port = int(sys.argv[2])
    wawa = mygang.MyGang(port, name)
    wawa.loop()