import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import re
import sys


# Main script
count=0;
values = [];
index = [];
with open("/home/bade/Downloads/cracker_zipf_1000000_seqRead_2000merge_seqRead_8000_2962503.out") as fp:
    for line in fp:
        #m = re.search('.*READ \(\d+\)',line)
        m = re.search('(?<=READ \()\w+',line)
        keyvalue = line.split(':')
        index.append(keyvalue[0])
        values.append(keyvalue[1].rstrip())

       
fig, ax1 = plt.subplots()
ax1.plot(index, values, 'b-')
ax1.set_yscale('log')


plt.show();
