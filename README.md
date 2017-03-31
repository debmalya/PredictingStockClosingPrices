# How to run

## Prerequisites
* JDK 8
* Maven 3.0

## Run
Extract attached source.
execute run.sh 

it will take stock_closing_prices.csv from src/main/resources and creates output file src/main/resources/predicted_prices.csv.


### Closing Price prediction
Calculate the price change for each stock for all the entries. Price change is calculated as today's closing price - previous day closing price.Get average change price. Add average change price with last closing price to get the next closing price.

### Purchase Decision
Sort the average price change. For purchase decision consider the stocks where average price change value is greater than zero. Take from the highest average price change. Allocate stock of total value less than equal to 10 lakh to it. Continue this process till have
some investment amount left.



