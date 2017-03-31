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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author debmalyajash
 *
 */
public class PredictClosingPriceTest {

	@Test
	public void test() {
		try {
			PredictClosingPrice.predictPrice("./src/main/resources/stocks_closing_prices.csv",
					"./src/main/resources/predicted_prices.csv");
		} catch (Throwable th) {
			th.printStackTrace();
			Assert.assertFalse(true);
		}
	}

}
