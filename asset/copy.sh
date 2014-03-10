#!/bin/sh

for dpi in {m,h,xh,xxh,xxxh}dpi; do
	mkdir -p ../res/drawable-${dpi}
	rm -rf ../res/drawable-${dpi}/ic_launcher.png
	#cp ./ic_launcher-${dpi}.png ../res/drawable-${dpi}/ic_launcher.png
	optipng ./ic_launcher-${dpi}.png -out ../res/drawable-${dpi}/ic_launcher.png
done
