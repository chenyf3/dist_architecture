package com.xpay.starter.logging.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

/**
 * 以 Marker 名称前缀作为判断依据的 Filter
 * @author chenyf
 */
@Plugin(name = "MarkerPrefixFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
@PerformanceSensitive("allocation")
public class MarkerPrefixFilter extends AbstractFilter {
    public static final String ATTR_MARKER = "prefix";
    private final String prefix;

    private MarkerPrefixFilter(final String prefix, final Result onMatch, final Result onMismatch) {
        super(onMatch, onMismatch);
        this.prefix = prefix;
    }

    @Override
    public Result filter(LogEvent event) {
        return filter(event.getMarker());
    }

    @Override
    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return filter(marker);
    }

    @Override
    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return filter(marker);
    }

    @Override
    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object... params) {
        return filter(marker);
    }

    private Result filter(final Marker marker) {
        return marker != null && marker.getName().startsWith(prefix) ? onMatch : onMismatch;
    }

    @PluginFactory
    public static MarkerPrefixFilter createFilter(
            @PluginAttribute(ATTR_MARKER) final String prefix,
            @PluginAttribute(value="onMatch", defaultString="ACCEPT") final Result match,
            @PluginAttribute(value="onMismatch", defaultString="NEUTRAL") final Result mismatch) {
        if (prefix == null) {
            LOGGER.error("A prefix must be provided for MarkerFilter");
            return null;
        }
        return new MarkerPrefixFilter(prefix, match, mismatch);
    }
}
