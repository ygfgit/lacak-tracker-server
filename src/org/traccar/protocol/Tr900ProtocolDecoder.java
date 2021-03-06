/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;

import org.jboss.netty.channel.Channel;
import org.traccar.BaseProtocolDecoder;
import org.traccar.helper.DateBuilder;
import org.traccar.helper.Parser;
import org.traccar.helper.PatternBuilder;
import org.traccar.model.Position;

public class Tr900ProtocolDecoder extends BaseProtocolDecoder {
  
  public Tr900ProtocolDecoder(Tr900Protocol protocol) {
    super(protocol);
  }
  
  private static final Pattern PATTERN = new PatternBuilder().number(">(d+),") // id
      .number("d+,") // period
      .number("(d),") // fix
      .number("(dd)(dd)(dd),") // date (yymmdd)
      .number("(dd)(dd)(dd),") // time
      .expression("([EW])").number("(ddd)(dd.d+),") // longitude
      .expression("([NS])").number("(dd)(dd.d+),") // latitude
      .expression("[^,]*,") // command
      .number("(d+.?d*),") // speed
      .number("(d+.?d*),") // course
      .number("(d+),") // gsm
      .number("(d+),") // event
      .number("(d+)-") // adc
      .number("(d+),") // battery
      .number("d+,") // impulses
      .number("(d+),") // input
      .number("(d+)") // status
      .any().compile();
  
  @Override
  protected Object decode(Channel channel, SocketAddress remoteAddress,
      Object msg) throws Exception {
    
    Parser parser = new Parser(PATTERN, (String) msg);
    if (!parser.matches()) {
      return null;
    }
    
    Position position = new Position();
    position.setProtocol(getProtocolName());
    
    if (!identify(parser.next(), channel, remoteAddress)) {
      return null;
    }
    position.setDeviceId(getDeviceId());
    
    position.setValid(parser.nextInt() == 1);
    
    DateBuilder dateBuilder = new DateBuilder().setDate(parser.nextInt(),
        parser.nextInt(), parser.nextInt()).setTime(parser.nextInt(),
        parser.nextInt(), parser.nextInt());
    position.setTime(dateBuilder.getDate());
    
    position.setLongitude(parser
        .nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
    position.setLatitude(parser
        .nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
    position.setSpeed(parser.nextDouble());
    position.setCourse(parser.nextDouble());
    
    position.set(Position.KEY_GSM, parser.next());
    position.set(Position.KEY_EVENT, parser.nextInt());
    position.set(Position.PREFIX_ADC + 1, parser.nextInt());
    position.set(Position.KEY_BATTERY, parser.nextInt());
    position.set(Position.KEY_INPUT, parser.next());
    position.set(Position.KEY_STATUS, parser.next());
    
    return position;
  }
  
}
