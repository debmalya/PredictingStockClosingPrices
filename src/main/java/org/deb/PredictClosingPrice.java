/**
 * Copyright 2015-2016 Debmalya Jash
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deb;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * @author debmalyajash
 *
 */
public class PredictClosingPrice {

	public static long totalInvestment = 10000000;

	public static long eachStockCap = 1000000;

	public static int minimumStocks = 10;

	private static final Logger LOGGER = Logger.getLogger(PredictClosingPrice.class);

	private static TreeMap<Double, List<String>> purchaseDecider = new TreeMap<>();

	/**
	 * @param args
	 *            Closing prices of 395 stocks are given for 1230 days. have to
	 *            predict the price for 1231, 1232, 1233 th day. Have to invest
	 */
	public static void main(String[] args) {
		if (args.length > 1) {
			predictPrice(args[0], args[1]);
		} else {
			System.err.println("Usage : java org.deb.PredictClosingPrice <input file name> <output file name>");
		}

	}

	/**
	 * @param filename
	 *            to read.
	 */
	public static void predictPrice(String inputFileName, String outputFileName) {
		CSVWriter writer = null;
		CSVReader reader = null;

		LinkedHashMap<String, List<Double>> stockMap = new LinkedHashMap<>();

		try {
			reader = new CSVReader(new FileReader(inputFileName));

			writer = new CSVWriter(new FileWriter(outputFileName), ',', '\0');
			String[] header = new String[] { "Stock_ID", "Closing_Price_1231", "Closing_Price_1232",
					"Closing_Price_1233", "Quantity" };
			writer.writeNext(header);

			String[] eachLine = reader.readNext();
			boolean firstLine = true;
			while (eachLine != null) {
				if (!firstLine) {
					List<Double> priceList = stockMap.get(eachLine[2]);
					if (priceList == null) {
						priceList = new ArrayList<>();
					}
					priceList.add(Double.parseDouble(eachLine[3]));
					stockMap.put(eachLine[2], priceList);

				} else {
					firstLine = false;
				}
				eachLine = reader.readNext();
			}

			List<String[]> closingEntries = new ArrayList<>();
			Set<Entry<String, List<Double>>> stockEntries = stockMap.entrySet();
			Iterator<Entry<String, List<Double>>> stockIterator = stockEntries.iterator();

			// Closing price calculation
			while (stockIterator.hasNext()) {
				Entry<String, List<Double>> nextStock = stockIterator.next();
				closingEntries.add(calculate(nextStock.getKey(), nextStock.getValue()));

			}

			// Take the stocks whose average change price is greater than zero
			SortedMap<Double, List<String>> purchaseMap = purchaseDecider.subMap(-1000000.00, 1000000.00);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Stocks to be considered :" + purchaseMap.size());
				LOGGER.debug(purchaseMap.firstKey());
				LOGGER.debug(purchaseMap.lastKey());
				LOGGER.debug(purchaseMap.get(purchaseMap.firstKey()));
			}

			int purchaseCount = 0;
			while (totalInvestment > 0 && purchaseMap.size() > 0) {
				double highest = purchaseMap.lastKey();
				List<String> stocks = purchaseMap.get(highest);
				for (String eachStockId : stocks) {
					double lastClosingPrice = 0.00;
					int quantity = 0;

					String[] stockDetails = null;

					for (int i = 0; i < closingEntries.size(); i++) {
						if (closingEntries.get(i)[0].equals(eachStockId)) {

							List<Double> allPrices = stockMap.get(eachStockId);
							stockDetails = closingEntries.get(i);
							lastClosingPrice = allPrices.get(allPrices.size() -1);
							if (totalInvestment < eachStockCap) {
								quantity = (int) (totalInvestment / lastClosingPrice);
							} else {
								quantity = (int) (eachStockCap / lastClosingPrice);
							}
							totalInvestment -= (quantity * lastClosingPrice);
							stockDetails[4] = String.valueOf(quantity);
							if (quantity > 0) {
								purchaseCount++;
							}
							closingEntries.set(i, stockDetails);
							if (purchaseCount > 10 && totalInvestment < 10000) {
								totalInvestment = 0;
								break;
							}
							break;
						}
					}

					purchaseMap.remove(highest);

				}
			}
			writer.writeAll(closingEntries);

		} catch (Throwable th) {
			th.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	/**
	 * @param value
	 * @return
	 */
	private static String[] calculate(String stockId, List<Double> value) {
		String[] values = new String[5];

		double avgChange = 0.00;
		double avgPrice = 0.00;
		double movingAvgPrice = 0.00;
		double movingAvgPriceTotal = 0.00;
		double previous = Double.MIN_VALUE;

		int count = 1;
		List<Double> movingAverage = new ArrayList<>();
		for (double each : value) {
			if (previous != Double.MIN_VALUE) {
				avgChange += (previous  - each) ;
			}
			if (count % 100 == 0) {
				double currentMovingAvgPrice = movingAvgPrice / 100;
				movingAvgPriceTotal += currentMovingAvgPrice;
				movingAverage.add(currentMovingAvgPrice);
				movingAvgPrice = 0;
			}
			avgPrice += each;
			movingAvgPrice += each;
			previous = each;
			count++;
		}

		avgPrice /= value.size();
		avgChange /= value.size() - 1;
		
		double currentMovingAvgPrice = movingAvgPrice / (count %100);
		movingAvgPriceTotal += currentMovingAvgPrice;
		movingAverage.add(currentMovingAvgPrice);
		movingAvgPrice = movingAvgPriceTotal / movingAverage.size();
		
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Avg price :"+ avgPrice + " Moving avg price :" + movingAvgPrice);
		}

		double closing1 = value.get(value.size() - 1) + avgChange;
		double closing2 = closing1 + avgChange;
		double closing3 = closing2 + avgChange;

		values[0] = stockId;
		values[1] = Double.toString(closing1);
		values[2] = Double.toString(closing2);
		values[3] = Double.toString(closing3);
		values[4] = "0";
//		values[5] = Double.toString(value.get(value.size() - 1));

		double quotient = avgChange;
		List<String> stockIds = purchaseDecider.get(quotient);
		if (stockIds == null) {
			stockIds = new ArrayList<>();
		}
		if (!stockIds.contains(stockId)) {
			stockIds.add(stockId);
		}
		purchaseDecider.put(quotient, stockIds);
		
		

		return values;
	}

	static class Stock {
		List<Double> priceList;
		int incrementCount;
		int decrementCount;
	}

}
