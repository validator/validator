// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values.external;

/**
 * From https://www.microsoft.com/typography/otspec/languagetags.htm
 * Retrieved 11 September 2021
 */
public class OpenTypeLanguageSystemTag {

    public static final String[] deprecated_tags = {"DHV "};

    public static final String[] tags = {
            "ABA ", "ABK ", "ACH ", "ACR ", "ADY ", "AFK ", "AFR ", "AGW ", "AIO ",
            "AKA ", "AKB ", "ALS ", "ALT ", "AMH ", "ANG ", "APPH", "ARA ", "ARG ",
            "ARI ", "ARK ", "ASM ", "AST ", "ATH ", "AVN ", "AVR ", "AWA ", "AYM ",
            "AZB ", "AZE ", "BAD ", "BAD0", "BAG ", "BAL ", "BAN ", "BAR ", "BAU ",
            "BBC ", "BBR ", "BCH ", "BCR ", "BDY ", "BEL ", "BEM ", "BEN ", "BGC ",
            "BGQ ", "BGR ", "BHI ", "BHO ", "BIK ", "BIL ", "BIS ", "BJJ ", "BKF ",
            "BLI ", "BLK ", "BLN ", "BLT ", "BMB ", "BML ", "BOS ", "BPY ", "BRE ",
            "BRH ", "BRI ", "BRM ", "BRX ", "BSH ", "BSK ", "BTD ", "BTI ", "BTK ",
            "BTM ", "BTS ", "BTX ", "BTZ ", "BUG ", "BYV ", "CAK ", "CAT ", "CBK ",
            "CCHN", "CEB ", "CGG ", "CHA ", "CHE ", "CHG ", "CHH ", "CHI ", "CHK ",
            "CHK0", "CHO ", "CHP ", "CHR ", "CHU ", "CHY ", "CJA ", "CJM ", "CMR ",
            "COP ", "COR ", "COS ", "CPP ", "CRE ", "CRR ", "CRT ", "CSB ", "CSL ",
            "CSY ", "CTG ", "CTT ", "CUK ", "DAG ", "DAN ", "DAR ", "DAX ", "DCR ",
            "DEU ", "DGO ", "DGR ", "DHG ", "DHV ", "DIQ ", "DIV ", "DJR ", "DJR0",
            "DNG ", "DNJ ", "DNK ", "DRI ", "DUJ ", "DUN ", "DZN ", "EBI ", "ECR ",
            "EDO ", "EFI ", "ELL ", "EMK ", "ENG ", "ERZ ", "ESP ", "ESU ", "ETI ",
            "EUQ ", "EVK ", "EVN ", "EWE ", "FAN ", "FAN0", "FAR ", "FAT ", "FIN ",
            "FJI ", "FLE ", "FMP ", "FNE ", "FON ", "FOS ", "FRA ", "FRC ", "FRI ",
            "FRL ", "FRP ", "FTA ", "FUL ", "FUV ", "GAD ", "GAE ", "GAG ", "GAL ",
            "GAR ", "GAW ", "GEZ ", "GIH ", "GIL ", "GIL0", "GKP ", "GLK ", "GMZ ",
            "GNN ", "GOG ", "GON ", "GRN ", "GRO ", "GUA ", "GUC ", "GUF ", "GUJ ",
            "GUZ ", "HAI ", "HAI0", "HAL ", "HAR ", "HAU ", "HAW ", "HAY ", "HAZ ",
            "HBN ", "HEI ", "HER ", "HIL ", "HIN ", "HMA ", "HMD ", "HMN ", "HMO ",
            "HMZ ", "HND ", "HO  ", "HRI ", "HRV ", "HUN ", "HYE ", "HYE0", "IBA ",
            "IBB ", "IBO ", "IDO ", "IJO ", "ILE ", "ILO ", "INA ", "IND ", "ING ",
            "INU ", "IPK ", "IPPH", "IRI ", "IRT ", "IRU ", "ISL ", "ISM ", "ITA ",
            "IWR ", "JAM ", "JAN ", "JAV ", "JBO ", "JCT ", "JII ", "JUD ", "JUL ",
            "KAB ", "KAB0", "KAC ", "KAL ", "KAN ", "KAR ", "KAT ", "KAW ", "KAZ ",
            "KDE ", "KEA ", "KEB ", "KEK ", "KGE ", "KHA ", "KHK ", "KHM ", "KHS ",
            "KHT ", "KHV ", "KHW ", "KIK ", "KIR ", "KIS ", "KIU ", "KJD ", "KJP ",
            "KJZ ", "KKN ", "KLM ", "KMB ", "KMN ", "KMO ", "KMS ", "KMZ ", "KNR ",
            "KOD ", "KOH ", "KOK ", "KOM ", "KON ", "KON0", "KOP ", "KOR ", "KOS ",
            "KOZ ", "KPL ", "KRI ", "KRK ", "KRL ", "KRM ", "KRN ", "KRT ", "KSH ",
            "KSH0", "KSI ", "KSM ", "KSW ", "KUA ", "KUI ", "KUL ", "KUM ", "KUR ",
            "KUU ", "KUY ", "KWK ", "KYK ", "KYU ", "LAD ", "LAH ", "LAK ", "LAM ",
            "LAO ", "LAT ", "LAZ ", "LCR ", "LDK ", "LEF ", "LEZ ", "LIJ ", "LIM ",
            "LIN ", "LIS ", "LJP ", "LKI ", "LMA ", "LMB ", "LMO ", "LMW ", "LOM ",
            "LPO ", "LRC ", "LSB ", "LSM ", "LTH ", "LTZ ", "LUA ", "LUB ", "LUG ",
            "LUH ", "LUO ", "LVI ", "MAD ", "MAG ", "MAH ", "MAJ ", "MAK ", "MAL ",
            "MAM ", "MAN ", "MAP ", "MAR ", "MAW ", "MBN ", "MBO ", "MCH ", "MCR ",
            "MDE ", "MDR ", "MEN ", "MER ", "MFA ", "MFE ", "MIN ", "MIZ ", "MKD ",
            "MKR ", "MKW ", "MLE ", "MLG ", "MLN ", "MLR ", "MLY ", "MND ", "MNG ",
            "MNI ", "MNK ", "MNX ", "MOH ", "MOK ", "MOL ", "MON ", "MOR ", "MOS ",
            "MRI ", "MTH ", "MTS ", "MUN ", "MUS ", "MWL ", "MWW ", "MYN ", "MZN ",
            "NAG ", "NAH ", "NAN ", "NAP ", "NAS ", "NAU ", "NAV ", "NCR ", "NDB ",
            "NDC ", "NDG ", "NDS ", "NEP ", "NEW ", "NGA ", "NGR ", "NHC ", "NIS ",
            "NIU ", "NKL ", "NKO ", "NLD ", "NOE ", "NOG ", "NOR ", "NOV ", "NSM ",
            "NSO ", "NTA ", "NTO ", "NYM ", "NYN ", "NZA ", "OCI ", "OCR ", "OJB ",
            "ORI ", "ORO ", "OSS ", "PAA ", "PAG ", "PAL ", "PAM ", "PAN ", "PAP ",
            "PAP0", "PAS ", "PAU ", "PCC ", "PCD ", "PDC ", "PGR ", "PHK ", "PIH ",
            "PIL ", "PLG ", "PLK ", "PMS ", "PNB ", "POH ", "PON ", "PRO ", "PTG ",
            "PWO ", "QIN ", "QUC ", "QUH ", "QUZ ", "QVI ", "QWH ", "RAJ ", "RAR ",
            "RBU ", "RCR ", "REJ ", "RIA ", "RHG ", "RIF ", "RIT ", "RKW ", "RMS ",
            "RMY ", "ROM ", "ROY ", "RSY ", "RTM ", "RUA ", "RUN ", "RUP ", "RUS ",
            "SAD ", "SAN ", "SAS ", "SAT ", "SAY ", "SCN ", "SCO ", "SCS ", "SEK ",
            "SEL ", "SFM ", "SGA ", "SGO ", "SGS ", "SHI ", "SHN ", "SIB ", "SID ",
            "SIG ", "SKS ", "SKY ", "SLA ", "SLV ", "SML ", "SMO ", "SNA ", "SNA0",
            "SND ", "SNH ", "SNK ", "SOG ", "SOP ", "SOT ", "SQI ", "SRB ", "SRD ",
            "SRK ", "SRR ", "SSL ", "SSM ", "STQ ", "SUK ", "SUN ", "SUR ", "SVA ",
            "SVE ", "SWA ", "SWK ", "SWZ ", "SXT ", "SXU ", "SYL ", "SYR ", "SYRE",
            "SYRJ", "SYRN", "SZL ", "TAB ", "TAJ ", "TAM ", "TAT ", "TCR ", "TDD ",
            "TEL ", "TET ", "TGL ", "TGN ", "TGR ", "TGY ", "THA ", "THT ", "TIB ",
            "TIV ", "TKM ", "TLI ", "TMH ", "TMN ", "TNA ", "TNE ", "TNG ", "TOD ",
            "TOD0", "TPI ", "TRK ", "TSG ", "TSJ ", "TUA ", "TUL ", "TUM ", "TUV ",
            "TVL ", "TWI ", "TYZ ", "TZM ", "TZO ", "UDM ", "UKR ", "UMB ", "URD ",
            "USB ", "UYG ", "UZB ", "VEC ", "VEN ", "VIT ", "VOL ", "VRO ", "WA  ",
            "WAG ", "WAR ", "WCI ", "WCR ", "WEL ", "WLF ", "WLN ", "WTM ", "XBD ",
            "XHS ", "XJB ", "XKF ", "XOG ", "XPE ", "XUB ", "XUJ ", "YAK ", "YAO ",
            "YAP ", "YBA ", "YCR ", "YGP ", "YIC ", "YIM ", "YNA ", "YWQ ", "ZEA ",
            "ZGH ", "ZHA ", "ZHH ", "ZHP ", "ZHS ", "ZHT ", "ZHTM", "ZND ", "ZUL ",
            "ZZA ",
    };
}
