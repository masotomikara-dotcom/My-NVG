This is a personal project aimed at learning and improving Java coding skills. 
I chose Android Native because it's neither too easy nor too difficult, and it trains me better.
I created this app because I wanted to create a digital NVG, but I failed with the Military Insurgency mod when I was working on that NVG.
It doesn't actually amplify light like a true NVG lens because the camera hardware always returns a result of 0 if environment is too dark. 
Because it is 0, the multiplication algorithms can no longer accurately project the light.
I once tried an algorithm I called "noise," which would randomly adjust the light from 0.1 to 0.8 for dots below the given limit, but it only created chaotic, bright dots and didn't display anything.
Therefore, if you want to create pixel brightness (white proximity) multiplication algorithms that are as good as real glasses, you have to use an infrared camera or some other method that is not a simple multiplication algorithm.
