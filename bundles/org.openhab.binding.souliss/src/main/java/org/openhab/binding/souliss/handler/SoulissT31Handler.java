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

package org.openhab.binding.souliss.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.internal.HalfFloatUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SoulissT31Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissT31Handler extends SoulissGenericHandler {
    private @NonNullByDefault({}) Configuration gwConfigurationMap;

    DecimalType setPointValue = DecimalType.ZERO;
    StringType fanStateValue = StringType.EMPTY;
    StringType powerState = StringType.EMPTY;
    StringType fireState = StringType.EMPTY;

    StringType lastModeState = StringType.EMPTY;
    StringType modeStateValue = StringType.EMPTY;

    DecimalType setMeasuredValue = DecimalType.ZERO;

    public SoulissT31Handler(Thing pThing) {
        super(pThing);
        thing = pThing;
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            switch (channelUID.getId()) {
                // FAN
                case SoulissBindingConstants.T31_SYSTEM_CHANNEL:
                    if (command.equals(OnOffType.OFF)) {
                        commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_SHUTDOWN);
                    } else {
                        if (modeStateValue.toString()
                                .equals(SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL)) {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_HEATING);
                        } else {
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_COOLING);
                        }
                    }
                    break;
                case SoulissBindingConstants.T31_MODE_CHANNEL:
                    if (command.toString().equals(SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL)) {
                        commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_HEATING);
                    } else {
                        commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_COOLING);
                    }
                    break;
                case SoulissBindingConstants.T31_BUTTON_CHANNEL:
                    if (command.equals(OnOffType.ON)) {
                        commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_AS_MEASURED);
                    }
                    break;
                case SoulissBindingConstants.T31_FAN_CHANNEL:
                    switch (command.toString()) {
                        case SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_FAN_MANUAL);
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_FAN_HIGH);
                            fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL);
                            break;
                        case SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_FAN_MANUAL);
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_FAN_MED);
                            fanStateValue = StringType
                                    .valueOf(SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL);
                            break;
                        case SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_FAN_MANUAL);
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_FAN_LOW);
                            fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL);
                            break;
                        case SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_FAN_AUTO);
                            fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL);
                            break;
                        case SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL:
                            commandSEND(SoulissBindingProtocolConstants.SOULISS_T3N_FAN_OFF);
                            fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL);
                            break;
                    }
                    break;
                case SoulissBindingConstants.T31_SETPOINT_CHANNEL:
                    if (command instanceof DecimalType) {
                        int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
                        byte b2 = (byte) (uu >> 8);
                        byte b1 = (byte) uu;
                        // setpoint command
                        commandSEND(SoulissBindingProtocolConstants.SOULISS_T31_USE_OF_SLOT_SETPOINT_COMMAND, b1, b2);
                    }
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

        gwConfigurationMap = thingGeneric.getConfiguration();
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = ((Boolean) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND)).booleanValue();
        }
    }

    public void setState(PrimitiveType state) {
        this.updateState(SoulissBindingConstants.T31_BUTTON_CHANNEL, OnOffType.OFF);

        super.setLastStatusStored();
        if (state instanceof StringType) {
            switch (state.toString()) {
                case SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL:
                case SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL:
                case SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL:
                case SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL:
                case SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL:
                    if (!fanStateValue.equals(state)) {
                        this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) state);
                        fanStateValue = (StringType) state;
                    }
                    break;

                case SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL:
                case SoulissBindingConstants.T31_COOLINGMODE_MESSAGE_MODE_CHANNEL:
                    if (!modeStateValue.equals(state)) {
                        this.updateState(SoulissBindingConstants.T31_MODE_CHANNEL, (StringType) state);
                        modeStateValue = (StringType) state;
                    }
                    break;

                case SoulissBindingConstants.T31_OFF_MESSAGE_SYSTEM_CHANNEL:
                    if (!powerState.equals(state)) {
                        this.updateState(SoulissBindingConstants.T31_SYSTEM_CHANNEL, OnOffType.OFF);
                        powerState = (StringType) state;
                    }
                    break;
                case SoulissBindingConstants.T31_ON_MESSAGE_SYSTEM_CHANNEL:
                    if (!powerState.equals(state)) {
                        this.updateState(SoulissBindingConstants.T31_SYSTEM_CHANNEL, OnOffType.ON);
                        powerState = (StringType) state;
                    }
                    break;

                case SoulissBindingConstants.T31_ON_MESSAGE_FIRE_CHANNEL:
                    if (!fireState.equals(state)) {
                        this.updateState(SoulissBindingConstants.T31_FIRE_CHANNEL, OnOffType.ON);
                        powerState = (StringType) state;
                    }
                    break;
                case SoulissBindingConstants.T31_OFF_MESSAGE_FIRE_CHANNEL:
                    if (!fireState.equals(state)) {
                        this.updateState(SoulissBindingConstants.T31_FIRE_CHANNEL, OnOffType.OFF);
                        powerState = (StringType) state;
                    }
                    break;
            }

        }
    }

    public void setMeasuredValue(DecimalType valueOf) {
        if (valueOf instanceof DecimalType) {
            if (!setMeasuredValue.equals(valueOf)) {
                this.updateState(SoulissBindingConstants.T31_VALUE_CHANNEL, valueOf);
                setMeasuredValue = valueOf;
            }
        }
    }

    public void setSetpointValue(DecimalType valueOf) {
        if (valueOf instanceof DecimalType) {
            if (!setPointValue.equals(valueOf)) {
                this.updateState(SoulissBindingConstants.T31_SETPOINT_CHANNEL, valueOf);
                setPointValue = valueOf;
            }
        }
    }

    public void setRawStateValues(byte rawStateByte0, float valTemp, float valSetPoint) {
        String sMessage = "";
        switch (getBitState(rawStateByte0, 0)) {
            case 0:
                sMessage = SoulissBindingConstants.T31_OFF_MESSAGE_SYSTEM_CHANNEL;
                break;
            case 1:
                sMessage = SoulissBindingConstants.T31_ON_MESSAGE_SYSTEM_CHANNEL;
                break;
        }
        this.setState(StringType.valueOf(sMessage));

        switch (getBitState(rawStateByte0, 7)) {
            case 0:
                sMessage = SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL;
                break;
            case 1:
                sMessage = SoulissBindingConstants.T31_COOLINGMODE_MESSAGE_MODE_CHANNEL;
                break;
        }
        this.setState(StringType.valueOf(sMessage));

        // button indicante se il sistema sta andando o meno
        switch (getBitState(rawStateByte0, 1) + getBitState(rawStateByte0, 2)) {
            case 0:
                sMessage = SoulissBindingConstants.T31_OFF_MESSAGE_FIRE_CHANNEL;
                break;
            case 1:
                sMessage = SoulissBindingConstants.T31_ON_MESSAGE_FIRE_CHANNEL;
                break;
        }
        this.setState(StringType.valueOf(sMessage));

        // FAN SPEED
        switch (getBitState(rawStateByte0, 3) + getBitState(rawStateByte0, 4) + getBitState(rawStateByte0, 5)) {
            case 0:
                sMessage = SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL;
                break;
            case 1:
                sMessage = SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL;
                break;
            case 2:
                sMessage = SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL;
                break;
            case 3:
                sMessage = SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL;
                break;
        }

        this.setState(StringType.valueOf(sMessage));

        // SLOT 1-2: Temperature Value
        if (!Float.isNaN(valTemp)) {
            this.setMeasuredValue(DecimalType.valueOf(String.valueOf(valTemp)));
        }

        // SLOT 3-4: Setpoint Value
        if (!Float.isNaN(valSetPoint)) {
            this.setSetpointValue(DecimalType.valueOf(String.valueOf(valSetPoint)));
        }
    }

    @Override
    public byte getRawState() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // TODO Auto-generated method stub
        return 0;
    }

    public byte getBitState(byte vRaw, int iBit) {
        final int maskBit1 = 0x1;

        if (((vRaw >>> iBit) & maskBit1) == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void setRawState(byte rawState) {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }
}
