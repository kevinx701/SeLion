/*-------------------------------------------------------------------------------------------------------------------*\
|  Copyright (C) 2014 PayPal                                                                                          |
|                                                                                                                     |
|  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     |
|  with the License.                                                                                                  |
|                                                                                                                     |
|  You may obtain a copy of the License at                                                                            |
|                                                                                                                     |
|       http://www.apache.org/licenses/LICENSE-2.0                                                                    |
|                                                                                                                     |
|  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   |
|  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  |
|  the specific language governing permissions and limitations under the License.                                     |
\*-------------------------------------------------------------------------------------------------------------------*/

package com.paypal.selion.reports.runtime;

import com.paypal.selion.configuration.Config;
import com.paypal.selion.logger.SeLionLogger;
import com.paypal.selion.platform.grid.Grid;
import com.paypal.test.utilities.logging.SimpleLogger;
import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.*;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;
import ru.yandex.qatools.ashot.shooting.cutter.VariableCutStrategy;

import java.awt.image.BufferedImage;
import java.util.logging.Level;

class Gatherer {
    
    private Gatherer() {
        // Utility class. So hide the constructor
    }
    
    private static SimpleLogger logger = SeLionLogger.getLogger();
    
    // this is applicable for both web and mobile. In mobile, you can use safari, etc, to test mobile webpages and
    // getCurrentUrl will get the webpage URL. If testing a mobile application instead of mobile webbrowser then
    // getCurrentURL will throw Exception  
    static String saveGetLocation(WebDriver driver) {
        logger.entering(driver);
        String location = "n/a";
        try {
            if (driver != null) {
                location = driver.getCurrentUrl();
            }
        } catch (Exception exception) {
            logger.log(Level.FINER, "Current location couldn't be retrieved by getCurrentUrl(). This can be SAFELY "
                    + "IGNORED if testing a non-web mobile application. Reason: ", exception);
        }
        logger.exiting(location);
        return location;
    }
    
    static byte[] takeScreenshot(WebDriver driver) {
        logger.entering(driver);
        try {
            byte[] decodeBuffer = null;
            
            if (driver instanceof TakesScreenshot) {
                TakesScreenshot screenshot = ((TakesScreenshot) driver);
                String ss = screenshot.getScreenshotAs(OutputType.BASE64);
                decodeBuffer = Base64.decodeBase64(ss.getBytes());
            }
            logger.exiting(decodeBuffer);
            return decodeBuffer;
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Screenshot couldn't be retrieved by getScreenshotAs().", exception);
            return null;
        }
    }
    
    static BufferedImage takeStrategyScreenshot(WebDriver driver) {
        String headerClasses = null;
        String footerClasses = null;
        Boolean hideBizHeader = false;
        Boolean hidePersonalHeader = false;
        Boolean hideBizFooter = false;
        Boolean hidePersonalFooter = false;
        WebElement merchantHeader = null;
        WebElement merchantFooter = null;
        WebElement personalHeader = null;
        WebElement personalFooter = null;
        Dimension size = Grid.driver().manage().window().getSize();
        int headerHeight = 0;
        int footerHeight = 0;
        int viewportHeight = size.getHeight();
        logger.entering(driver);
        try {
           
            try {
                merchantHeader = Grid.driver()
                        .findElementByCssSelector(Config.getConfigProperty(Config.ConfigProperty.MERCHANT_HEADER));
                merchantFooter = Grid.driver()
                        .findElementByCssSelector(Config.getConfigProperty(Config.ConfigProperty.MERCHANT_FOOTER));
                personalHeader = Grid.driver()
                        .findElementByCssSelector(Config.getConfigProperty(Config.ConfigProperty.PERSONAL_HEADER));
                personalFooter = Grid.driver()
                        .findElementByCssSelector(Config.getConfigProperty(Config.ConfigProperty.PERSONAL_FOOTER));
            } catch (NoSuchElementException e) {
            
            }
            
            if (merchantHeader != null && merchantHeader.isDisplayed()) {
                headerClasses = merchantHeader.getAttribute("class");
                headerHeight = merchantHeader.getSize().getHeight();
                Grid.driver().executeScript("arguments[0]..className = ’" + headerClasses + " hide'", merchantHeader);
                hideBizHeader = true;
            }
    
            if (merchantFooter != null && merchantFooter.isDisplayed()) {
                footerClasses = merchantFooter.getAttribute("class");
                footerHeight = merchantFooter.getSize().getHeight();
                Grid.driver().executeScript("arguments[0]..className = ’" + footerClasses + " hide'", merchantHeader);
                hideBizFooter = true;
            }
            
            if (personalHeader != null && personalHeader.isDisplayed()) {
                headerClasses = personalHeader.getAttribute("class");
                headerHeight = personalHeader.getSize().getHeight();
                Grid.driver().executeScript("arguments[0]..className = ’" + headerClasses + " hide'", personalHeader);
                hidePersonalHeader = true;
            }
            
            if (personalFooter != null && personalFooter.isDisplayed()) {
                footerClasses = personalFooter.getAttribute("class");
                footerHeight = personalFooter.getSize().getHeight();
                Grid.driver().executeScript("arguments[0]..className = ’" + footerClasses + " hide'", personalFooter);
                hidePersonalFooter = true;
            }
            
            CutStrategy cutting = new VariableCutStrategy(0, headerHeight, 0, footerHeight,
                    viewportHeight - headerHeight - footerHeight);
            ShootingStrategy shootingStrategies = new ViewportPastingDecorator(ShootingStrategies.cutting(cutting));
            Screenshot aShot = new AShot().shootingStrategy(shootingStrategies)
                    .takeScreenshot(Grid.driver(), headerHeight, footerHeight);
            return aShot.getImage();
            
        } catch (Exception exception) {
            logger.log(Level.WARNING, "Screenshot couldn't be retrieved by getScreenshotAs().", exception);
            return null;
        } finally {
            if (hideBizHeader) {
                Grid.driver().executeScript("arguments[0]..className = ‘" + headerClasses + "'", merchantHeader);
            }
    
            if (hideBizFooter) {
                Grid.driver().executeScript("arguments[0]..className = ‘" + footerClasses + "'", merchantFooter);
            }
    
            if (hidePersonalHeader) {
                Grid.driver().executeScript("arguments[0]..className = ‘" + headerClasses + "'", personalHeader);
            }
    
            if (hidePersonalFooter) {
                Grid.driver().executeScript("arguments[0]..className = ‘" + footerClasses + "'", personalFooter);
            }
        }
    }
    
}
