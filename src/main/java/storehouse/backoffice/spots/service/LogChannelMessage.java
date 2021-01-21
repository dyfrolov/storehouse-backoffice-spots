package storehouse.backoffice.spots.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import storehouse.backoffice.spots.api.dto.DtoLog;
import storehouse.backoffice.spots.interfaces.ILogChannelMessage;

@Slf4j
@Component
public class LogChannelMessage implements ILogChannelMessage {
	@Autowired
	ObjectMapper mapper;

	@Override
	public String formatLogString(LogMessageType type, String serviceName, String info) {
		String strLog = String.format("%s %s [%s]  %s", LocalDateTime.now().toString(), type.toString(), serviceName,
				info);
		String resultStr = null;
		try {
			resultStr = mapper.writeValueAsString(new DtoLog(strLog));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			resultStr = String.format("{\"log\":\"%s\"}", strLog);
		}
		return resultStr;
	}

}
