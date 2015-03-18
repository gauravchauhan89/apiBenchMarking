set terminal jpeg size 500,500
set size 1, 1
set output "graphs/{DATAFILE}.jpg"
set title "Benchmark testing"
set key left top
set grid y
set datafile separator '\t'
stats "{DATAFILE}.txt" every ::2 using 2:5 nooutput
set xdata time
set timefmt "%s"
set format x " %M:%S"
set xlabel 'seconds'
set ylabel "response time (ms)"
set ytics add (sprintf('%.1f', STATS_mean_y) STATS_mean_y)
plot "{DATAFILE}.txt" every ::2 using 2:5 title 'response time' with points, STATS_mean_y title " Mean"
exit