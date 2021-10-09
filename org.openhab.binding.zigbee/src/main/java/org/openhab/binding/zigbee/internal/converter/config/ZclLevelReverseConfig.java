/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zigbee.internal.converter.config;

import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.zsmartsystems.zigbee.zcl.ZclCluster;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;
import org.slf4j.LoggerFactory;

/**
 * Configuration handler for inverting commands sent to and from the Level Control channel.
 * This enhances support for Zigbee devices that use the LEVEL_CONTROL cluster for window coverings.
 *
 * @author Colin Keehan
 *
 */
public class ZclLevelReverseConfig implements ZclClusterConfigHandler {
    private final Logger logger = LoggerFactory.getLogger(ZclLevelReverseConfig.class);

    private static final Boolean REVERSE_ONOFF_DEFAULT = false;
    private static final Boolean REVERSE_PERCENT_DEFAULT = false;

    private static final String CONFIG_ID = "zigbee_levelreverse_";
    private static final String CONFIG_REVERSEONOFF = CONFIG_ID + "reverseonoff";
    private static final String CONFIG_REVERSEPERCENT = CONFIG_ID + "reversepercent";

    private boolean reverseOnOff = REVERSE_ONOFF_DEFAULT;
    private boolean reversePercent = REVERSE_PERCENT_DEFAULT;
    private int reportingChange = 1;

    public ZclLevelReverseConfig(Channel channel) {
        Configuration configuration = channel.getConfiguration();
        if (configuration.containsKey(CONFIG_REVERSEONOFF)) {
            reverseOnOff = ((Boolean) configuration.get(CONFIG_REVERSEONOFF));
        }
        if (configuration.containsKey(CONFIG_REVERSEPERCENT)) {
            reversePercent = ((Boolean) configuration.get(CONFIG_REVERSEPERCENT));
        }
    }

    @Override
    public boolean initialize(ZclCluster cluster) {
        return true;
    }

    @Override
    public List<ConfigDescriptionParameter> getConfiguration() {
        List<ConfigDescriptionParameter> parameters = new ArrayList<>();

        // Build a list of configuration
        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_REVERSEONOFF, Type.BOOLEAN)
                .withLabel("Invert On/Off Commands")
                .withDescription("Invert the value of ON and OFF commands sent to and received from device. Useful for devices that use the OnOffCluster for rollershutter control.")
                .withDefault(REVERSE_ONOFF_DEFAULT.toString())
                .build());

        parameters.add(ConfigDescriptionParameterBuilder.create(CONFIG_REVERSEPERCENT, Type.BOOLEAN)
                .withLabel("Invert Percent Commands")
                .withDescription("Invert the value of percent commands sent to and received from device. Useful for devices that use the LevelControlCluster for rollershutter control.")
                .withDefault(REVERSE_PERCENT_DEFAULT.toString())
                .build());

        return parameters;
    }

    @Override
    public boolean updateConfiguration(@NonNull Configuration currentConfiguration,
            Map<String, Object> configurationParameters) {

        boolean updated = false;
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            if (!configurationParameter.getKey().startsWith(CONFIG_ID)) {
                continue;
            }
            // Ignore any configuration parameters that have not changed
            if (Objects.equals(configurationParameter.getValue(),
                    currentConfiguration.get(configurationParameter.getKey()))) {
                logger.debug("Configuration update: Ignored {} as no change", configurationParameter.getKey());
                continue;
            }

            switch (configurationParameter.getKey()) {
                case CONFIG_REVERSEONOFF:
                    reverseOnOff = ((Boolean) (configurationParameter.getValue()));
                    updated = true;
                    break;
                case CONFIG_REVERSEPERCENT:
                    reversePercent = ((Boolean) (configurationParameter.getValue()));
                    updated = true;
                    break;
                default:
                    logger.warn("Unhandled configuration property {}", configurationParameter.getKey());
                    break;
            }
        }

        return updated;
    }

    /**
     * Inverts on/off command value
     *
     * @return inverted OnOffType value
     */
    public OnOffType invertCommand(OnOffType cmdOnOff) {
        OnOffType reverseOnOff;
        if (cmdOnOff == OnOffType.ON) {
            reverseOnOff = OnOffType.OFF;
        } else {
            reverseOnOff = OnOffType.ON;
        }
        return reverseOnOff;
    }

    /**
     * Inverts up/down command value
     *
     * @return inverted UpDownType value
     */
    public UpDownType invertCommand(UpDownType cmdUpDown) {
        UpDownType reverseUpDown;
        if (cmdUpDown == UpDownType.UP) {
            reverseUpDown = UpDownType.DOWN;
        } else {
            reverseUpDown = UpDownType.UP;
        }
        return reverseUpDown;
    }

    /**
     * Inverts true/false command value
     *
     * @return inverted Boolean value
     */
    public boolean invertCommand(boolean cmdTrueFalse) {
        boolean reverseTrueFalse;
        if (cmdTrueFalse) {
            reverseTrueFalse = false;
        } else {
            reverseTrueFalse = true;
        }
        return reverseTrueFalse;
    }

    /**
     * Inverts percent command value
     *
     * @return inverted PercentType value
     */
    public PercentType invertCommand(PercentType cmdPercent) {
        PercentType reversePercent;
        reversePercent = new PercentType(100 - cmdPercent.intValue());
        return reversePercent;
    }

    /**
     * Gets channel configuration for inverting on/off commands
     *
     * @return boolean value of channel configuration
     */
    public boolean shouldInvertOnOff() {
        return reverseOnOff;
    }

    /**
     * Gets channel configuration for inverting on/off commands
     *
     * @return boolean value of channel configuration
     */
    public boolean shouldInvertPercent() {
        return reversePercent;
    }

    /**
     * Gets the reporting change configuration
     *
     * @return the reporting change parameter
     */
    public int getReportingChange() {
        return reportingChange;
    }
}
