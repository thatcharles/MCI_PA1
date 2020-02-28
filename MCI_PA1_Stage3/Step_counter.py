
# coding: utf-8

# In[1]:


"""
<Basic Step counter Algorithm>
1. Gathering Accelerometer X,Y,Z data
2. Applying a low-pass filter with Sample frequency 2Hz to eliminate random noises 
  (2Hz = 12.5663706 Rad/s)
3. Finding peak with three parameters; Height, prominence, and distance
  - Height: Minimal magnitude to count it as step. We can eliminate the small magnitude noises.
  - Prominence: Minimal magnitude difference with left and right valleys. Values depends on situatlion.
  - Distance: Minimal distacne from peak to peak. We will not count the steps too frequently. 
    (Normaly 100 which means 200ms = 5Hz. I assume that human cannot walk faster than this)
"""

import os
import glob
import csv
import math
import numpy as np
import matplotlib.pyplot as plt
import datetime
import scipy as sp
from scipy.signal import butter, lfilter, freqs
import pandas as pd
from scipy.signal import find_peaks

# Google drive setting
from google.colab import drive
drive.mount('/content/gdrive')
get_ipython().system('cd "/content/gdrive/My Drive/Colab Notebooks"')
data_file_path = ""


# In[2]:


csv_files_walk = glob.glob('/content/gdrive/My Drive/Colab Notebooks/WALK*.csv')
csv_files_run = glob.glob('/content/gdrive/My Drive/Colab Notebooks/RUN*.csv')
csv_files_jump = glob.glob('/content/gdrive/My Drive/Colab Notebooks/JUMP*.csv')
csv_files_idle = glob.glob('/content/gdrive/My Drive/Colab Notebooks/IDLE*.csv')
csv_files_stair = glob.glob('/content/gdrive/My Drive/Colab Notebooks/STAIR*.csv')
csv_files= glob.glob('/content/gdrive/My Drive/Colab Notebooks/*.csv')

data = dict() #plain python dictonary  to contain list of 3 axis list
pdata = dict() #dict of numpy arrays

for data_file_path in csv_files:
    data[data_file_path] = []
    First_flag=0
    with open(data_file_path, 'r') as f:
        reader = csv.reader(f, delimiter=',')
        for row in reader: #skip first row and timestamp field
          if (First_flag <1) :
            First_flag+=1
            continue
          if (row[1] != ""):
            data[data_file_path].append([float(row[1]),float(row[2]),float(row[3])]) # remove gravity
    pdata[data_file_path] = np.array(data[data_file_path])        
    plt.plot(pdata[data_file_path])
    plt.title(data_file_path)
    plt.figure(figsize=(10,5))
    plt.show()


# In[ ]:


mag=dict()
for data_file_path in csv_files:
    mag[data_file_path]=[]
    for ax in pdata[data_file_path]:
        mag[data_file_path].append(math.sqrt(np.sum(ax*ax)))

def butter_lowpass(cutoff, fs, order=5):
    nyq = 0.5 * fs
    normal_cutoff = cutoff / nyq
    b, a = butter(order, normal_cutoff, btype='lowpass', analog=False)
    return b, a

def butter_lowpass_filter(data, cutoff, fs, order=5):
    b, a = butter_lowpass(cutoff, fs, order=order)
    y = lfilter(b, a, data)
    return y


# In[4]:


#<Walking Analysis>
# Using sampling frequency 10 to LPF
# we can find the step value by changing value of prominence 4 or Height 12.
cutOff = .3 #cutoff frequency in rad/s
fs =  12.5663706 #sampling frequency in rad/s
order = 10 #order of filter

for data_file_path in csv_files_walk:
    y = butter_lowpass_filter((mag[data_file_path]), cutOff, fs, order)
    y = y- np.average(y)
    indices = find_peaks(y,height=1,prominence=3,distance=100)[0]
    plt.figure(figsize=(8,6))
    plt.plot(y,'-rD',markevery= list(indices))
    #plt.plot(mag[data_file_path])
    plt.title('<Walk> '+data_file_path+ ' '+ str(len(indices)))
    plt.tight_layout()
    plt.show()
    print("******.   Step Count=",len(indices),"(Real:13 steps) ************")


# In[5]:


#<Running Analysis>
# When we run, sensor values are far more fluctuating than when we walk.
# Appropriate value for prominence is 20 which is higher than walking case.
# Many peaks are showing up, but actual running steps are half of them.
# I think that accelerometer experiences bounce-back from impact of steping on the ground.
# So I increase the distance variable from 100 to 300

cutOff = 0.3 #cutoff frequency in rad/s
fs =  12.5663706 #sampling frequency in rad/s (2Hz)
order = 10 #order of filter

for data_file_path in csv_files_run:
    y = butter_lowpass_filter((mag[data_file_path]), cutOff, fs, order)
    y = y- np.average(y)
    indices = find_peaks(y,height=1,distance=300,prominence=10)[0]
    plt.figure(figsize=(8,6))
    plt.plot(y,'-rD',markevery= list(indices))
    #plt.plot(mag[data_file_path])
    plt.title('<RUN> '+data_file_path+ ' '+ str(len(indices)))
    plt.tight_layout()
    plt.show()
    print("******.   Step Count=",len(indices),"(Real:8 steps ************")


# In[6]:


##<Jumping Analysis>
# Using sampling frequency 10 to LPF
# We can find the appropriate step value by changing value of prominence to 10.
cutOff = 0.3 #cutoff frequency in rad/s
fs =  12.5663706 #sampling frequency in rad/s (2Hz)
order = 10 #order of filter

for data_file_path in csv_files_jump:
    y = butter_lowpass_filter((mag[data_file_path]), cutOff, fs, order)
    y = y- np.average(y)
    indices = find_peaks(y,height=1,distance=100,prominence=10)[0]
    plt.figure(figsize=(8,6))
    plt.plot(y,'-rD',markevery= list(indices))
    #plt.plot(mag[data_file_path])
    plt.title('<JUMP> '+data_file_path+ ' '+ str(len(indices)))
    plt.tight_layout()
    plt.show()
    print("******.   Step Count=",len(indices)," (Real:16 steps)************")


# In[7]:


#<STAIR Analysis>
# We can find the appropriate step value by changing value of prominence to 8 or height to 15
# <IDEA to count step on stair "not implemented yet" >
# 1. When go downstairs, We surely can find the point that accelerometer becomes zero.
# 2. Using barometer
# 3. To distinguish whether we go upstairs or downstiars, we can use pitch Gyro data to check that we lean forward or backward

cutOff = 0.3 #cutoff frequency in rad/s
fs =  12.5663706 #sampling frequency in rad/s (2Hz)
order = 10 #order of filter

for data_file_path in csv_files_stair:
    y = butter_lowpass_filter((mag[data_file_path]), cutOff, fs, order)
    y = y- np.average(y)
    indices = find_peaks(y,height=1,distance=100,prominence=6)[0]
    plt.figure(figsize=(8,6))
    plt.plot(y,'-rD',markevery= list(indices))
    #plt.plot(mag[data_file_path])
    plt.title('<STAIR> '+data_file_path+ ' '+ str(len(indices)))
    plt.tight_layout()
    plt.show()
    print("******.   Step Count=",len(indices),"(Real:18 steps) ************")


# In[8]:


#<IDLE Analysis>
# Using sampling frequency 10 to LPF
# We can find the appropriate step value by changing value of prominence to 20 or height to 25
cutOff = 0.3 #cutoff frequency in rad/s
fs = 10 #sampling frequency in rad/s
order = 10 #order of filter

for data_file_path in csv_files_idle:
    y = butter_lowpass_filter((mag[data_file_path]), cutOff, fs, order)
    y = y- np.average(y)
    indices = find_peaks(y,height=1,distance=100,prominence=6)[0]
    plt.figure(figsize=(8,6))
    plt.plot(y,'-rD',markevery= list(indices))
    #plt.plot(mag[data_file_path])
    plt.title('<IDLE> '+data_file_path+ ' '+ str(len(indices)))
    plt.tight_layout()
    plt.show()
    print("******.   Step Count=",len(indices)," (Real:0 steps)************")

