import {
  FileFormat,
} from './models/file-format';
import {
  AutoAdjustmentsPain00x,
  AutoAdjustmentsSwift, UserSettings
} from './models/user-settings';
import { uuid } from 'vue-uuid';
import { BankConnectionProperty } from './models/ebics-bank-connection';

/**
 * Text Utils composition API
 * @returns
 *  findAndReplace generic replacement function for aceEditor
 *  findAndReplaceMsgIds function replacing MsgIds
 */
export default function useTextUtils() {
  /**
   * Generic find & replace
   * @param input input string
   * @param regExp reg expression which is to be searched
   * @param replaceWith function
   * @returns the @param input with all replacements applied
   */
  const findAndReplace = (
    input: string,
    regExp: RegExp,
    replaceWith: (match: string) => string
  ): Promise<string> => {
    return new Promise<string>((resolve) => {
      resolve(input.replace(regExp, replaceWith));
    });
  };

  /**
   *
   * @returns current date in YYYY-MM-DD format 
   * @param dash - if true format is ISO: YYYY-MM-DD, otherwise: YYYYMMDD
   * @param date - optionally the concrete date, if not desired actual date
   */
  const formatDate = (dash = true, date: Date = new Date()): string => {
    //const date = new Date();
    return `${date.getFullYear()}${dash ? '-' : ''}${(date.getMonth() + 1)
      .toString()
      .padStart(2, '0')}${dash ? '-' : ''}${date
      .getDate()
      .toString()
      .padStart(2, '0')}`;
  };

  /**
   *
   * @returns unique timestamp within 1 year in MMDD-hhmmss format
   */
  const uniqueTimeStamp = (dash = true) => {
    const date = new Date();
    return `${(date.getMonth() + 1).toString().padStart(2, '0')}${date
      .getDate()
      .toString()
      .padStart(2, '0')}${dash ? '-' : ''}${date
      .getHours()
      .toString()
      .padStart(2, '0')}${date.getMinutes().toString().padStart(2, '0')}${date
      .getSeconds()
      .toString()
      .padStart(2, '0')}`;
  };

  const detectFileFormat = (fileContent: string): FileFormat => {
    //Detect binary - open binary data in a normal way (without using a hex editor),
    //it will encounter some rendering problems which translate to you as a succession
    //of this weird character � called "Replacement character" == ufffd
    if (/\ufffd/.test(fileContent) === true) {
      return FileFormat.BINARY;
    } else {
      if (
        fileContent.includes('xmlns') ||
        fileContent.includes('<?xml') ||
        fileContent.includes('<Document')
      )
        return FileFormat.XML;
      else if (
        fileContent.includes('{1:') ||
        fileContent.includes('{2:') ||
        fileContent.includes('{3:') ||
        fileContent.includes('{4:')
      )
        return FileFormat.SWIFT;
      else return FileFormat.TEXT;
    }
  };

  const getFileExtension = (fileFormat: FileFormat): string => {
    switch (fileFormat) {
      case FileFormat.BINARY:
        return 'zip';
      case FileFormat.XML:
        return 'xml';
      default:
        return 'txt';
    }
  };

  const applySmartAdjustments = async (
    fileText: string,
    fileFormat: FileFormat,
    settings: UserSettings
  ): Promise<string> => {
    switch (fileFormat) {
      case FileFormat.BINARY:
      case FileFormat.TEXT:
      case FileFormat.SWIFT:
        return await applySmartAdjustmentsSwift(
          fileText,
          settings.adjustmentOptions.swift
        );
        break;
      case FileFormat.XML:
        return await applySmartAdjustmentsPain00x(
          fileText,
          settings.adjustmentOptions.pain00x
        );
    }
  };

  const makeUniqueId = (length: number): string => {
    let result = '';
    const characters =
      'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const charactersLength = characters.length;
    for (let i = 0; i < length; i++) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
  };

  function trimString(str: string): string {
    if (str.length <= 16) return str;
    else if (str.length) return str.slice(str.length - 16);
    else return str;
  }

  const applyTemplateAdjustments = async (
    fileText: string,
    fileFormat: FileFormat,
    settings: UserSettings,
    bankConnectionProperties: BankConnectionProperty[]
  ): Promise<string> => {
    console.log(`applyTemplateAdjustments: ${fileFormat} props:${JSON.stringify(bankConnectionProperties)}`)
    switch (fileFormat) {
      case FileFormat.BINARY:
      case FileFormat.TEXT:
      case FileFormat.SWIFT:
        return await applySmartAdjustmentsSwift(
          fileText,
          settings.adjustmentOptions.swift
        );
        break;
      case FileFormat.XML:
        return await applyTemplateAdjustmentsPain001(
          fileText,
          settings.adjustmentOptions.pain00x,
          bankConnectionProperties
        );
    }
  };

  const applyTemplateAdjustmentsPain001 = async (fileText: string, s: AutoAdjustmentsPain00x, bankConnectionProperties: BankConnectionProperty[]): Promise<string> => {
    const regExpParts = ['(%%aLevelId%%)', '(%%bLevelId(-keep)?%%)', '(%%cLevelId(-keep)?%%)', '(%%UETR(-keep)?%%)', 
        '(%%IsoDtTm([+-]\d+)?%%)', '(%%IsoDt([+-]\d+)?%%)', '(%%.{3,30}?%%)',
        '(<InstdAmt Ccy="\\w{3}">.*<\\/InstdAmt>)|(<Amt Ccy="\\w{3}">.*<\\/Amt>)'
    ];
    const idPrefix = s.idPrefix + '-' + uniqueTimeStamp();
    let bLevel = 0;
    let cLevel = 0;
    let uetr = uuid.v4();
    let nbOfTrxs = 0;
    let ctrlSum = 0;
    const regExp = new RegExp(regExpParts.join('|'), 'g');
    console.log(regExp.source);

    const text = await findAndReplace(fileText, regExp, (match) => {
      if (match.startsWith('%%aLevelId%%')) {
        return idPrefix;
      }
      else if (match.startsWith('%%bLevelId')) {
        if (!match.includes('-keep')) {
          cLevel = 0
          bLevel++
        }
        return `${idPrefix}-B${bLevel}`;
      }
      else if (match.startsWith('%%cLevelId')) {
        if (!match.includes('-keep')) {
          cLevel++
          nbOfTrxs++
        }
        return `${idPrefix}-B${bLevel}-C${cLevel}`;
      }
      else if (match.startsWith('%%IsoDtTm')) {
        const date = new Date()
        if (match.charAt(9) == '+' || match.charAt(9) == '-') {
          const daysString = match.substring(9, match.length - 2)
          if (Number.isInteger(daysString)) {
            const daysOffset = Number(daysString);
            date.setDate(date.getDate() + daysOffset);
          }
        }
        return date.toISOString()
      }
      else if (match.startsWith('%%IsoDt')) {
        const date = new Date()
        if (match.charAt(7) == '+' || match.charAt(7) == '-') {
          const daysString = match.substring(7, match.length - 2)
          if (Number.isInteger(daysString)) {
            const daysOffset = Number(daysString);
            date.setDate(date.getDate() + daysOffset);
          }
        }
        return formatDate(true, date)
      }
      else if (match.startsWith('%%UETR')) {
        if (!match.includes('-keep')) {
          uetr = uuid.v4();
        }
        return uetr;
      } else if ((match.startsWith('<InstdAmt') || match.startsWith('<Amt'))) {
        const amt = Number(
          match.substring(match.indexOf('>') + 1, match.lastIndexOf('<'))
        );
        ctrlSum += amt;
        return match;
      } else if (match.startsWith('%%') && match.endsWith('%%')) {
        const propertyKeyName = match.substring(2, match.length - 2)
        const bankConnectionProperty = bankConnectionProperties.find(bankConnectionProperty => bankConnectionProperty.key == propertyKeyName);
        if (bankConnectionProperty) {
          return bankConnectionProperty.value
        } else {
          return match
        }
      } else {
        return 'unknown match'
      }
    });

    return await findAndReplace(
      text,
      new RegExp('(%%NbOfTxs-aLevel%%)|(%%CtrlSum%%)', 'g'),
      (match) => {
        if (match.startsWith('%%NbOfTxs-aLevel%%')) {
          return `${nbOfTrxs}`;
        } else if (match.startsWith('%%CtrlSum%%')) {
          return ctrlSum
            .toFixed(5)
            .replace(new RegExp('(\\.)?0+$'), '');
        } else { 
          return 'Unknown match';
        }
      }
    );
  }

  const applySmartAdjustmentsSwift = async (
    fileText: string,
    settings: AutoAdjustmentsSwift
  ): Promise<string> => {
    const s = settings;
    const idPrefix = s.idPrefix + uniqueTimeStamp(false);
    let cLevel = 1;
    const regExpParts = [
      s.f20 ? '(:20:.*)' : null,
      s.f21 ? '(:21:.*)' : null,
      s.f30 ? '(:30:.*)' : null,
      s.uetr ? '({121:.*?})' : null,
    ].filter(Boolean);
    if (regExpParts.length) {
      const regExp = new RegExp(regExpParts.join('|'), 'g');
      console.log(regExp.source);
      const text = await findAndReplace(fileText, regExp, (match) => {
        if (s.f20 && match.startsWith(':20:')) {
          return `:20:${
            s.randomIds
              ? makeUniqueId(16)
              : trimString(idPrefix + cLevel.toString())
          }`;
        }
        if (s.f21 && match.startsWith(':21:')) {
          const res = `:21:${
            s.randomIds
              ? makeUniqueId(16)
              : trimString(idPrefix + cLevel.toString())
          }`;
          cLevel++;
          return res;
        }
        if (s.f30 && match.startsWith(':30')) {
          return `:30:${formatDate(false).slice(2)}`;
        }
        if (s.uetr && match.startsWith('{121')) {
          return `{121:${uuid.v4()}}`;
        } else {
          return 'unknown match';
        }
      });
      return text;
    } else {
      return fileText;
    }
  };

  const applySmartAdjustmentsPain00x = async (
    fileText: string,
    settings: AutoAdjustmentsPain00x
  ): Promise<string> => {
    const s = settings;
    const idPrefix = s.idPrefix + '-' + uniqueTimeStamp();
    let bLevel = 0;
    let cLevel = 0;
    let nbOfTrxs = 0;
    let ctrlSum = 0;
    const regExpParts = [
      s.pmtInfId || s.instrId || s.endToEndId
        ? '(<PmtInfId>.*</PmtInfId>)'
        : null,
      s.instrId ? '(<InstrId>.*</InstrId>)' : null,
      s.endToEndId || s.nbOfTrxsCalc ? '(<EndToEndId>.*</EndToEndId>)' : null,
      s.uetr ? '(<InstrInf>UETR/.*</InstrInf>)|(<UETR>.*</UETR>)' : null,
      s.ctrlSumCalc
        ? '(<InstdAmt Ccy="\\w{3}">.*<\\/InstdAmt>)|(<Amt Ccy="\\w{3}">.*<\\/Amt>)'
        : null,
      s.creDtTm ? '(<CreDtTm>.*<\\/CreDtTm>)' : null,
      s.reqdExctnDt ? '(<ReqdExctnDt>.*<\\/ReqdExctnDt>)' : null,
      s.msgId ? '(<MsgId>.*</MsgId>)' : null,
    ].filter(Boolean);
    let output = fileText;
    if (regExpParts.length) {
      const regExp = new RegExp(regExpParts.join('|'), 'g');
      output = await findAndReplace(fileText, regExp, (match) => {
        if (s.msgId && match.startsWith('<MsgId>')) {
          return `<MsgId>${idPrefix}</MsgId>`;
        } else if (match.startsWith('<PmtInfId>')) {
          cLevel = 0;
          bLevel++;
          return s.pmtInfId
            ? `<PmtInfId>${idPrefix}-B${bLevel}</PmtInfId>`
            : match;
        } else if (s.instrId && match.startsWith('<InstrId>')) {
          return `<InstrId>${idPrefix}-B${bLevel}-C${cLevel + 1}</InstrId>`;
        } else if (
          (s.endToEndId || s.nbOfTrxsCalc) &&
          match.startsWith('<EndToEndId>')
        ) {
          cLevel++;
          nbOfTrxs++;
          return s.endToEndId
            ? `<EndToEndId>${idPrefix}-B${bLevel}-C${cLevel}</EndToEndId>`
            : match;
        } else if (s.uetr && match.startsWith('<InstrInf>')) {
          return `<InstrInf>UETR/${uuid.v4()}</InstrInf>`;
        } else if (s.uetr && match.startsWith('<UETR>')) {
          return `<UETR>${uuid.v4()}</UETR>`;
        } else if (
          s.ctrlSumCalc &&
          (match.startsWith('<InstdAmt') || match.startsWith('<Amt'))
        ) {
          const amt = Number(
            match.substring(match.indexOf('>') + 1, match.lastIndexOf('<'))
          );
          ctrlSum += amt;
          return match;
        } else if (s.reqdExctnDt && match.startsWith('<ReqdExctnDt>')) {
          return `<ReqdExctnDt>${formatDate()}</ReqdExctnDt>`;
        } else if (s.creDtTm && match.startsWith('<CreDtTm>')) {
          return `<CreDtTm>${new Date().toISOString()}</CreDtTm>`;
        } else return 'Unknown match';
      });
    }
    if (s.nbOfTrxsCalc || s.ctrlSumCalc) {
      return await findAndReplace(
        output,
        new RegExp(
          (s.nbOfTrxsCalc ? '(<NbOfTxs>.*</NbOfTxs>)' : '') +
            (s.nbOfTrxsCalc && s.ctrlSumCalc ? '|' : '') +
            (s.ctrlSumCalc ? '(<CtrlSum>.*</CtrlSum>)' : ''),
          'g'
        ),
        (match) => {
          if (s.nbOfTrxsCalc && match.startsWith('<NbOfTxs>')) {
            return `<NbOfTxs>${nbOfTrxs}</NbOfTxs>`;
          } else if (s.ctrlSumCalc && match.startsWith('<CtrlSum>')) {
            return `<CtrlSum>${ctrlSum
              .toFixed(5)
              .replace(new RegExp('(\\.)?0+$'), '')}</CtrlSum>`;
          } else return 'Unknown match';
        }
      );
    } else {
      return output;
    }
  };

  return {
    applyTemplateAdjustments,
    detectFileFormat,
    currentDate: formatDate,
    uniqueTimeStamp,
    applySmartAdjustments,
    getFileExtension,
    makeUniqueId,
  };
}
