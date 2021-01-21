package storehouse.backoffice.spots.interfaces;

public interface ILogChannelMessage {
	String formatLogString(LogMessageType type, String serviceName, String info);

	enum LogMessageType {
		INFO, WARNING, ERROR
	}
}
