package com.greg.golf.controller.dto;

import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Locale;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

/**
 * Jackson (de)serializers for {@link Instant} fields that keep the JSON contract used when these fields were
 * {@code java.util.Date}: values are written in UTC, parsing is lenient (e.g. "2024/6/2" or "2024/02/30"),
 * trailing text is ignored, epoch milliseconds are accepted and an empty string means null.
 */
public final class UtcInstantJson {

	public static final String DATE_PATTERN = "yyyy/MM/dd";
	public static final String DATE_TIME_PATTERN = "yyyy/MM/dd kk:mm";

	private static final DateTimeFormatter DATE = lenientPattern(DATE_PATTERN);
	private static final DateTimeFormatter DATE_TIME = lenientPattern(DATE_TIME_PATTERN);

	private static final DateTimeFormatter ISO_PRINTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.ROOT).withZone(ZoneOffset.UTC);

	// ISO date with optional time and optional offset ("Z", "+02", "+0200" or "+02:00")
	private static final DateTimeFormatter ISO_PARSER = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ISO_LOCAL_DATE)
			.optionalStart()
			.appendLiteral('T')
			.append(DateTimeFormatter.ISO_LOCAL_TIME)
			.optionalStart()
			.parseLenient()
			.appendOffset("+HH:MM", "Z")
			.optionalEnd()
			.optionalEnd()
			.toFormatter(Locale.ROOT);

	private UtcInstantJson() {
	}

	private static DateTimeFormatter lenientPattern(String pattern) {
		return new DateTimeFormatterBuilder().parseLenient().appendPattern(pattern).toFormatter(Locale.ROOT)
				.withResolverStyle(ResolverStyle.LENIENT).withZone(ZoneOffset.UTC);
	}

	static Instant parse(DateTimeFormatter parser, String text) {
		TemporalAccessor parsed = parser.parse(text, new ParsePosition(0));
		LocalTime time = parsed.query(TemporalQueries.localTime());
		ZoneOffset offset = parsed.query(TemporalQueries.offset());
		return parsed.query(TemporalQueries.localDate())
				.atTime(time == null ? LocalTime.MIDNIGHT : time)
				.toInstant(offset == null ? ZoneOffset.UTC : offset)
				.truncatedTo(ChronoUnit.MILLIS);
	}

	private abstract static class InstantSerializer extends ValueSerializer<Instant> {

		private final DateTimeFormatter formatter;

		InstantSerializer(DateTimeFormatter formatter) {
			this.formatter = formatter;
		}

		@Override
		public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) {
			gen.writeString(formatter.format(value));
		}
	}

	private abstract static class InstantDeserializer extends ValueDeserializer<Instant> {

		private final DateTimeFormatter parser;

		InstantDeserializer(DateTimeFormatter parser) {
			this.parser = parser;
		}

		@Override
		public Instant deserialize(JsonParser p, DeserializationContext ctxt) {
			if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
				return Instant.ofEpochMilli(p.getLongValue());
			}
			if (!p.hasToken(JsonToken.VALUE_STRING)) {
				return (Instant) ctxt.handleUnexpectedToken(Instant.class, p);
			}
			String text = p.getString().trim();
			if (text.isEmpty()) {
				return null;
			}
			try {
				return parse(parser, text);
			} catch (DateTimeException e) {
				return (Instant) ctxt.handleWeirdStringValue(Instant.class, text, "%s", e.getMessage());
			}
		}
	}

	public static final class DateSerializer extends InstantSerializer {
		public DateSerializer() {
			super(DATE);
		}
	}

	public static final class DateDeserializer extends InstantDeserializer {
		public DateDeserializer() {
			super(DATE);
		}
	}

	public static final class DateTimeSerializer extends InstantSerializer {
		public DateTimeSerializer() {
			super(DATE_TIME);
		}
	}

	public static final class DateTimeDeserializer extends InstantDeserializer {
		public DateTimeDeserializer() {
			super(DATE_TIME);
		}
	}

	public static final class IsoSerializer extends InstantSerializer {
		public IsoSerializer() {
			super(ISO_PRINTER);
		}
	}

	public static final class IsoDeserializer extends InstantDeserializer {
		public IsoDeserializer() {
			super(ISO_PARSER);
		}
	}
}
