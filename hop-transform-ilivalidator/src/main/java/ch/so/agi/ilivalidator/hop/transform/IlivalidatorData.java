package ch.so.agi.ilivalidator.hop.transform;

import ch.so.agi.ilivalidator.core.validator.IlivalidatorOptionEntry;
import ch.so.agi.ilivalidator.core.validator.IlivalidatorResult;
import java.util.List;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

public class IlivalidatorData extends BaseTransformData implements ITransformData {

  IRowMeta outputRowMeta;
  int inputFilePathIndex = -1;
  int inputConfigFieldIndex = -1;
  int inputMetaConfigFieldIndex = -1;

  int outputIsValidIndex = -1;
  int outputValidationMessageIndex = -1;
  int outputLogFilePathIndex = -1;

  List<IlivalidatorOptionEntry> optionEntries;
  IlivalidatorResult cachedStaticResult;

  boolean initialized;
  boolean emittedSingleStaticRow;
}
