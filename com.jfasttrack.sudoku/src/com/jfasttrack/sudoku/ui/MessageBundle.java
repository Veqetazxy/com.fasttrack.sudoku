/*
  MessageBundle.java

  Copyright (C) 2008-2009 by Pete Boton, www.jfasttrack.com

  This file is part of Dancing Links Sudoku.

  Dancing Links Sudoku is free for non-commercial use. Contact the author for commercial use.

  You can redistribute and/or modify this software only under the terms of the GNU General Public
  License as published by the Free Software Foundation. Version 2 of the License or (at your option)
  any later version may be used.

  This program is distributed in the hope that it will be useful and enjoyable, but WITH NO
  WARRANTY; not even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this program; if not,
  write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307.
*/

package com.jfasttrack.sudoku.ui;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * This class contains a resource bundle that holds message string constants. The strings for the
 * default locale (U.S. English) are loaded when this class is loaded. Other message strings are
 * loaded as needed (i.e., when locale is changed).
 * <p>
 * This class is a singleton, implemented according to the singleton design pattern with eager
 * instantiation.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public final class MessageBundle {

    /** The singleton instance of this class. */
    private static final MessageBundle INSTANCE = new MessageBundle();

    /** The relative path to the resource containing all message strings. */
    private static final String PATH_TO_BUNDLE_FILE = "data/Messages";

    /**
     * Resource bundle that holds all strings in memory. By default, this
     * bundle holds strings for the <code>en_US</code> locale. It can be
     * changed by calling <code>setLocale</code>.
     */
    private ResourceBundle messages =
            ResourceBundle.getBundle(PATH_TO_BUNDLE_FILE, Locale.US);

    /** Private constructor, so nobody else can instantiate this class. */
    private MessageBundle() {
        // Nothing to do here.
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return  A reference to the singleton instance of this class.
     */
    public static MessageBundle getInstance() {
        return INSTANCE;
    }

    /**
     * Loads the <code>ResourceBundle</code> for a specified locale.
     *
     * @param language  2-character abbreviation of the language to be used.
     * @param country   2-character abbreviation of the country to be used.
     */
    public void setLocale(final String language, final String country) {
        setLocale(new Locale(language, country));
    }

    /**
     * Loads the <code>ResourceBundle</code> for a specified locale.
     *
     * @param locale  The new locale.
     */
    public void setLocale(final Locale locale) {
        messages = ResourceBundle.getBundle(PATH_TO_BUNDLE_FILE, locale);
    }

    /**
     * Gets the message that has the specified key.
     *
     * @param key  A key that uniquely identifies a message.
     * @return     The message that has the specified key.
     */
    public String getString(final String key) {
        return messages.getString(key);
    }

    /**
     * Gets the message that has the specified key. Replaces message pieces
     * as requested.
     * <p>
     * Messages can specify values to be filled in at run time. The text of
     * any message can contain indexed strings ("{0}", "{1}", and so on).
     * These substrings will be replaced (in order) by the values in the
     * parameters argument.
     *
     * @param key     A key that uniquely identifies a message.
     * @param values  Strings that can be used as replacement values within
     *                the retrieved message.
     * @return        The message that has the specified key, with values
     *                replaced as requested.
     */
    public String getString(final String key, final String[] values) {

        // Get the original message.
        StringBuffer message = new StringBuffer(messages.getString(key));

        // Perform replacements as requested.
        for (int i = 0; i < values.length; i++) {
            int startIndex = message.indexOf("{" + i + "}");
            int endIndex = startIndex + ("{" + i + "}").length();
            message.replace(startIndex, endIndex, values[i]);
        }

        return message.toString();
    }
}
