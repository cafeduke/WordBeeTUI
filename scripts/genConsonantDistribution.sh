#!/bin/bash

# Map of consonant to number of occurences
declare -A score
score=([b]=4 [c]=8 [d]=8 [f]=3 [g]=5 [h]=5 [j]=1 [k]=2 [l]=10 [m]=5 [n]=10 [p]=6 [q]=1 [r]=10 [s]=10 [t]=10 [v]=3 [w]=2 [x]=1 [y]=3 [z]=1)

str=""
for alpha in "${!score[@]}"
do
  alphaScore=${score[$alpha]}
  str="${str}"$(head -c $alphaScore < /dev/zero | tr '\0' "$alpha")
  # echo "Alpha=$alpha Score=$alphaScore"
  # echo "$str"
  # echo "--------------"
done

echo "$str" | rev
