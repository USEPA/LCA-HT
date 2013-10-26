package harmonizationtool.utils;

public class Util {
	private Util() {
	}

	public static String escape(String s) {
		char[] chars = s.toCharArray();
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\\') {
				b.append("\\");
			}
			if (chars[i] == '"') {
				b.append("\\");
			}
//			if (chars[i] == ',') {
//				b.append("\\");
//			}
//			if (chars[i] == '\'') {
//				b.append("\\");
//			}
//			if (chars[i] == '`') {
//				b.append("\\");
//			}
			b.append(chars[i]);
		}
		return b.toString();
	}

}
