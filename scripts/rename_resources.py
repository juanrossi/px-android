import os
import sys
import fnmatch
directory = os.path.dirname(os.path.realpath(sys.argv[0])) #get the directory of your script
for subdir, dirs, files in os.walk(directory):
 print(files)
 for filename in files:
  if fnmatch.fnmatch(filename,'mpsdk_*') > 0:
   subdirectoryPath = os.path.relpath(subdir, directory) #get the path to your subdirectory
   filePath = os.path.join(subdirectoryPath, filename) #get the path to your file
   newFilePath = filePath.replace("mpsdk_","px_") #create the new name
   os.rename(filePath, newFilePath) #rename your file
