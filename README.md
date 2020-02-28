# MCI_PA1

## Stage 1 & 2

The android app code is in **MCI_PA1_Stage1_2** folder.
	* The output CSV files are in **result** folder.

## Stage 3

In **MCI_PA1_Stage3** folder 
Step_counter.ipynb: Source code + Graph for Jupyter notebook version
Step_counter.py: Source code for python version
Step_counter.ipynb collaboratory.pdf: Jupyter notebook printed version

### Basic Step counter Algorithm
  1. Gathering Accelerometer X,Y,Z data
  2. Applying a low-pass filter with Sample frequency 2Hz to eliminate random noises 
    (2Hz = 12.5663706 Rad/s)
  3. Finding peak with three parameters; Height, prominence, and distance
    * Height: Minimal magnitude to count it as step. We can eliminate the small magnitude noises.
    * Prominence: Minimal magnitude difference with left and right valleys. Values depends on situatlion.
    * Distance: Minimal distacne from peak to peak. We will not count the steps too frequently. 
      (Normaly 100 which means 200ms = 5Hz. I assume that human cannot walk faster than this)


## Stage 4

The python code and output graphs are in **MCI_PA1_Stage4** folder
