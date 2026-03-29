package it.asso.core.common;

/**
 * IsNull
 *
 */
public final class IsNull {

  /**
   * 
   */
  private IsNull() {
    // metodo vuoto
  }

  /**
   * @param str
   * @return
   */
  public static String thenBlank(String str) {

    if (str == null) {
      return "";
    }
    return str.equals("null") ? "" : str;
  }

  /**
   * @param obj
   * @return
   */
  public static Object thenBlank(Object obj) {

    if (obj == null) {
      return "";
    }
    return obj;
  }

  /**
   * @param obj
   * @return
   */
  public static Object thenNull(Object obj) {

    if (obj == null) {
      return null;
    }
    return obj;
  }

  /**
   * @param str
   * @return
   */
  public static String thenZero(String str) {

    if (str == null || str.equals("")) {
      return "0";
    }
    return str;
  }

  /**
   * @param str
   * @return
   */
  public static int thenZeroInt(String str) {

    if (str == null || str.equals("")) {
      return 0;
    }
    return Integer.parseInt(str);
  }

  /**
   * @param str
   * @return
   */
  public static Double thenZeroDouble(Double d) {

    if (d == null) {
      return 0.0;
    }
    return d;
  }

  /**
   * @param str
   * @param value
   * @return
   */
  public static String thenValue(String str, String value) {

    if (str == null || str.equals("")) {
      return value;
    }
    return str;
  }

  /**
   * @param str
   * @return
   */
  public static String thenPercentage(String str) {

    if (str == null || "".equals(str)) {
      return "%";
    }
    return str;
  }

  /**
   * @param strBuffer
   * @return
   */
  public static StringBuilder thenStringBuilder(StringBuilder strBuffer) {

    if (strBuffer.toString().equals("null")) {
      return new StringBuilder();
    }
    return strBuffer;
  }

}
