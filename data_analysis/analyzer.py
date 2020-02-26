import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D  

def count_zc(data):

	count = 0
	positive = None
	for i in data:

		#init positive flag
		if positive is None:

			if(i>0): positive = True
			elif(i<0): positive = False
			else: positive = None

		else:

			if(positive and i<0):
				count += 1
			elif(not positive and i>0):
				count += 1
			else:
				continue

	return count

def plot_features(df, feature_1,feature_2, feature_3):


	fig = plt.figure()
	ax = fig.add_subplot(111, projection='3d')

	ax.scatter(df[feature_1].to_numpy(), df[feature_2].to_numpy(), df[feature_3].to_numpy())

	ax.set_xlabel(feature_1)
	ax.set_ylabel(feature_2)
	ax.set_zlabel(feature_3)

	plt.show()

features = [
'interval_start',
'mean_acc_x', 'median_acc_x', 'max_acc_x','zc_acc_x', 'var_acc_x',
'mean_acc_y', 'median_acc_y', 'max_acc_y','zc_acc_y', 'var_acc_y',
'mean_acc_z', 'median_acc_z', 'max_acc_z','zc_acc_z', 'var_acc_z',
'mean_gyr_x', 'median_gyr_x', 'max_gyr_x','zc_gyr_x', 'var_gyr_x',
'mean_gyr_y', 'median_gyr_y', 'max_gyr_y','zc_gyr_y', 'var_gyr_y',
'mean_gyr_z', 'median_gyr_z', 'max_gyr_z','zc_gyr_z', 'var_gyr_z',
'mean_mag_x', 'median_mag_x', 'max_mag_x','zc_mag_x', 'var_mag_x',
'mean_mag_y', 'median_mag_y', 'max_mag_y','zc_mag_y', 'var_mag_y',
'mean_mag_z', 'median_mag_z', 'max_mag_z','zc_mag_z', 'var_mag_z',
]

sensor_df = pd.read_csv('sensor_readings_corrected.csv')
sensor_df = sensor_df[sensor_df.Timestamp >= 0].drop(['Light Intensity'], axis=1)
print(sensor_df)

analyzed_df = pd.DataFrame(columns=features)

max_time = sensor_df['Timestamp'].max()

for i in range(0,max_time,200):

	new_row = []

	interval = sensor_df[sensor_df.Timestamp>=i]
	interval = interval[interval.Timestamp<(i+200)].drop(['Timestamp'], axis=1)

	new_row.append(i)
	for col in interval.columns:
		new_row.append(interval[col].mean())
		new_row.append(interval[col].median())
		new_row.append(interval[col].max())
		new_row.append(count_zc(interval[col].to_numpy()))
		new_row.append(np.var(interval[col]))

	analyzed_df = analyzed_df.append(pd.Series(new_row, index = analyzed_df.columns), ignore_index=True)

analyzed_df.to_csv('calculated_sensor_features.csv')
plot_features(analyzed_df,'mean_acc_x', 'median_acc_x', 'max_acc_x')