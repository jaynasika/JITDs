import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import re


# Main script
count=0;
values = [];
index = [];
with open('/home/jayanth/workspace/jitd/jitd-master/java/bin/results/demo_1.out') as fp:
    for line in fp:
        #m = re.search('.*READ \(\d+\)',line)
        m = re.search('(?<=READ \()\w+',line)
        #m = re.search('(?<=: )\w+',line)
        if m is None:
            count= count +1;
        else:
            #print m.group(0)
            index.append(m.group(0))
            n = re.search('(?<=: )\w+',line)
            if n is None:
                count= count +1;
            else:
                #print n.group(0)  
                val = float(n.group(0))/100000;
                values.append(val)          
                


#plt.plot(index,values,'b+');
#plt.show()

fig, ax1 = plt.subplots()
ax1.plot(index, values, 'b.')
ax1.set_yscale('log')

plt.show();