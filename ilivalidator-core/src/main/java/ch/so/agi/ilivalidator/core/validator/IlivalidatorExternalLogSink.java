package ch.so.agi.ilivalidator.core.validator;

@FunctionalInterface
public interface IlivalidatorExternalLogSink {
  void log(IlivalidatorExternalLogLevel level, String message, Throwable throwable);
}
