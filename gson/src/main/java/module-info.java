/**
 * Defines the Gson serialization/deserialization API.
 * @since 2.8.6
 */
module com.google.gson {
	exports tk.google.gson;
	exports tk.google.gson.annotations;
	exports tk.google.gson.reflect;
	exports tk.google.gson.stream;

	// Optional dependency on java.sql
	requires static java.sql;

	// Optional dependency on jdk.unsupported for JDK's sun.misc.Unsafe
	requires static jdk.unsupported;
}
