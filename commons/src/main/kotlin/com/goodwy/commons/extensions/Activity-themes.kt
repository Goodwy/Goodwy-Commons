package com.goodwy.commons.extensions

import android.app.Activity
import android.graphics.Color
import com.goodwy.commons.R
import com.goodwy.commons.helpers.DARK_GREY

fun Activity.getThemeId(color: Int = baseConfig.primaryColor, showTransparentTop: Boolean = false) = when {
    isDynamicTheme() -> if (isSystemInDarkMode()) R.style.AppTheme_Base_System else R.style.AppTheme_Base_System_Light

    showTransparentTop -> {
        when (color) {
            -12846 -> R.style.AppTheme_Red_100_core
            -1074534 -> R.style.AppTheme_Red_200_core
            -1739917 -> R.style.AppTheme_Red_300_core
            -1092784 -> R.style.AppTheme_Red_400_core
            -769226 -> R.style.AppTheme_Red_500_core
            -1754827 -> R.style.AppTheme_Red_600_core
            -2937041 -> R.style.AppTheme_Red_700_core
            -3790808 -> R.style.AppTheme_Red_800_core
            -4776932 -> R.style.AppTheme_Red_900_core

            -476208 -> R.style.AppTheme_Pink_100_core
            -749647 -> R.style.AppTheme_Pink_200_core
            -1023342 -> R.style.AppTheme_Pink_300_core
            -1294214 -> R.style.AppTheme_Pink_400_core
            -1499549 -> R.style.AppTheme_Pink_500_core
            -2614432 -> R.style.AppTheme_Pink_600_core
            -4056997 -> R.style.AppTheme_Pink_700_core
            -5434281 -> R.style.AppTheme_Pink_800_core
            -7860657 -> R.style.AppTheme_Pink_900_core

            -1982745 -> R.style.AppTheme_Purple_100_core
            -3238952 -> R.style.AppTheme_Purple_200_core
            -4560696 -> R.style.AppTheme_Purple_300_core
            -5552196 -> R.style.AppTheme_Purple_400_core
            -6543440 -> R.style.AppTheme_Purple_500_core
            -7461718 -> R.style.AppTheme_Purple_600_core
            -8708190 -> R.style.AppTheme_Purple_700_core
            -9823334 -> R.style.AppTheme_Purple_800_core
            -11922292 -> R.style.AppTheme_Purple_900_core

            -3029783 -> R.style.AppTheme_Deep_Purple_100_core
            -5005861 -> R.style.AppTheme_Deep_Purple_200_core
            -6982195 -> R.style.AppTheme_Deep_Purple_300_core
            -8497214 -> R.style.AppTheme_Deep_Purple_400_core
            -10011977 -> R.style.AppTheme_Deep_Purple_500_core
            -10603087 -> R.style.AppTheme_Deep_Purple_600_core
            -11457112 -> R.style.AppTheme_Deep_Purple_700_core
            -12245088 -> R.style.AppTheme_Deep_Purple_800_core
            -13558894 -> R.style.AppTheme_Deep_Purple_900_core

            -3814679 -> R.style.AppTheme_Indigo_100_core
            -6313766 -> R.style.AppTheme_Indigo_200_core
            -8812853 -> R.style.AppTheme_Indigo_300_core
            -10720320 -> R.style.AppTheme_Indigo_400_core
            -12627531 -> R.style.AppTheme_Indigo_500_core
            -13022805 -> R.style.AppTheme_Indigo_600_core
            -13615201 -> R.style.AppTheme_Indigo_700_core
            -14142061 -> R.style.AppTheme_Indigo_800_core
            -15064194 -> R.style.AppTheme_Indigo_900_core

            -4464901 -> R.style.AppTheme_Blue_100_core
            -7288071 -> R.style.AppTheme_Blue_200_core
            -10177034 -> R.style.AppTheme_Blue_300_core
            -12409355 -> R.style.AppTheme_Blue_400_core
            -14575885 -> R.style.AppTheme_Blue_500_core
            -14776091 -> R.style.AppTheme_Blue_600_core
            -15108398 -> R.style.AppTheme_Blue_700_core
            -15374912 -> R.style.AppTheme_Blue_800_core
            -15906911 -> R.style.AppTheme_Blue_900_core

            -4987396 -> R.style.AppTheme_Light_Blue_100_core
            -8268550 -> R.style.AppTheme_Light_Blue_200_core
            -11549705 -> R.style.AppTheme_Light_Blue_300_core
            -14043396 -> R.style.AppTheme_Light_Blue_400_core
            -16537100 -> R.style.AppTheme_Light_Blue_500_core
            -16540699 -> R.style.AppTheme_Light_Blue_600_core
            -16611119 -> R.style.AppTheme_Light_Blue_700_core
            -16615491 -> R.style.AppTheme_Light_Blue_800_core
            -16689253 -> R.style.AppTheme_Light_Blue_900_core

            -5051406 -> R.style.AppTheme_Cyan_100_core
            -8331542 -> R.style.AppTheme_Cyan_200_core
            -11677471 -> R.style.AppTheme_Cyan_300_core
            -14235942 -> R.style.AppTheme_Cyan_400_core
            -16728876 -> R.style.AppTheme_Cyan_500_core
            -16732991 -> R.style.AppTheme_Cyan_600_core
            -16738393 -> R.style.AppTheme_Cyan_700_core
            -16743537 -> R.style.AppTheme_Cyan_800_core
            -16752540 -> R.style.AppTheme_Cyan_900_core

            -5054501 -> R.style.AppTheme_Teal_100_core
            -8336444 -> R.style.AppTheme_Teal_200_core
            -11684180 -> R.style.AppTheme_Teal_300_core
            -14244198 -> R.style.AppTheme_Teal_400_core
            -16738680 -> R.style.AppTheme_Teal_500_core
            -16742021 -> R.style.AppTheme_Teal_600_core
            -16746133 -> R.style.AppTheme_Teal_700_core
            -16750244 -> R.style.AppTheme_Teal_800_core
            -16757440 -> R.style.AppTheme_Teal_900_core

            -3610935 -> R.style.AppTheme_Green_100_core
            -5908825 -> R.style.AppTheme_Green_200_core
            -8271996 -> R.style.AppTheme_Green_300_core
            -10044566 -> R.style.AppTheme_Green_400_core
            -11751600 -> R.style.AppTheme_Green_500_core
            -12345273 -> R.style.AppTheme_Green_600_core
            -13070788 -> R.style.AppTheme_Green_700_core
            -13730510 -> R.style.AppTheme_Green_800_core
            -14983648 -> R.style.AppTheme_Green_900_core

            -2298424 -> R.style.AppTheme_Light_Green_100_core
            -3808859 -> R.style.AppTheme_Light_Green_200_core
            -5319295 -> R.style.AppTheme_Light_Green_300_core
            -6501275 -> R.style.AppTheme_Light_Green_400_core
            -7617718 -> R.style.AppTheme_Light_Green_500_core
            -8604862 -> R.style.AppTheme_Light_Green_600_core
            -9920712 -> R.style.AppTheme_Light_Green_700_core
            -11171025 -> R.style.AppTheme_Light_Green_800_core
            -13407970 -> R.style.AppTheme_Light_Green_900_core

            -985917 -> R.style.AppTheme_Lime_100_core
            -1642852 -> R.style.AppTheme_Lime_200_core
            -2300043 -> R.style.AppTheme_Lime_300_core
            -2825897 -> R.style.AppTheme_Lime_400_core
            -3285959 -> R.style.AppTheme_Lime_500_core
            -4142541 -> R.style.AppTheme_Lime_600_core
            -5983189 -> R.style.AppTheme_Lime_700_core
            -6382300 -> R.style.AppTheme_Lime_800_core
            -8227049 -> R.style.AppTheme_Lime_900_core

            -1596 -> R.style.AppTheme_Yellow_100_core
            -2672 -> R.style.AppTheme_Yellow_200_core
            -3722 -> R.style.AppTheme_Yellow_300_core
            -4520 -> R.style.AppTheme_Yellow_400_core
            -5317 -> R.style.AppTheme_Yellow_500_core
            -141259 -> R.style.AppTheme_Yellow_600_core
            -278483 -> R.style.AppTheme_Yellow_700_core
            -415707 -> R.style.AppTheme_Yellow_800_core
            -688361 -> R.style.AppTheme_Yellow_900_core

            -4941 -> R.style.AppTheme_Amber_100_core
            -8062 -> R.style.AppTheme_Amber_200_core
            -10929 -> R.style.AppTheme_Amber_300_core
            -13784 -> R.style.AppTheme_Amber_400_core
            -16121 -> R.style.AppTheme_Amber_500_core
            -19712 -> R.style.AppTheme_Amber_600_core
            -24576 -> R.style.AppTheme_Amber_700_core
            -28928 -> R.style.AppTheme_Amber_800_core
            -37120 -> R.style.AppTheme_Amber_900_core

            -8014 -> R.style.AppTheme_Orange_100_core
            -13184 -> R.style.AppTheme_Orange_200_core
            -18611 -> R.style.AppTheme_Orange_300_core
            -22746 -> R.style.AppTheme_Orange_400_core
            -26624 -> R.style.AppTheme_Orange_500_core
            -291840 -> R.style.AppTheme_Orange_600_core
            -689152 -> R.style.AppTheme_Orange_700_core
            -1086464 -> R.style.AppTheme_Orange_800_core
            -1683200 -> R.style.AppTheme_Orange_900_core

            -13124 -> R.style.AppTheme_Deep_Orange_100_core
            -21615 -> R.style.AppTheme_Deep_Orange_200_core
            -30107 -> R.style.AppTheme_Deep_Orange_300_core
            -36797 -> R.style.AppTheme_Deep_Orange_400_core
            -43230 -> R.style.AppTheme_Deep_Orange_500_core
            -765666 -> R.style.AppTheme_Deep_Orange_600_core
            -1684967 -> R.style.AppTheme_Deep_Orange_700_core
            -2604267 -> R.style.AppTheme_Deep_Orange_800_core
            -4246004 -> R.style.AppTheme_Deep_Orange_900_core

            -2634552 -> R.style.AppTheme_Brown_100_core
            -4412764 -> R.style.AppTheme_Brown_200_core
            -6190977 -> R.style.AppTheme_Brown_300_core
            -7508381 -> R.style.AppTheme_Brown_400_core
            -8825528 -> R.style.AppTheme_Brown_500_core
            -9614271 -> R.style.AppTheme_Brown_600_core
            -10665929 -> R.style.AppTheme_Brown_700_core
            -11652050 -> R.style.AppTheme_Brown_800_core
            -12703965 -> R.style.AppTheme_Brown_900_core

            -3155748 -> R.style.AppTheme_Blue_Grey_100_core
            -5194811 -> R.style.AppTheme_Blue_Grey_200_core
            -7297874 -> R.style.AppTheme_Blue_Grey_300_core
            -8875876 -> R.style.AppTheme_Blue_Grey_400_core
            -10453621 -> R.style.AppTheme_Blue_Grey_500_core
            -11243910 -> R.style.AppTheme_Blue_Grey_600_core
            -12232092 -> R.style.AppTheme_Blue_Grey_700_core
            -13154481 -> R.style.AppTheme_Blue_Grey_800_core
            -14273992 -> R.style.AppTheme_Blue_Grey_900_core

            -1 -> R.style.AppTheme_Grey_100_core
            -1118482 -> R.style.AppTheme_Grey_200_core
            -2039584 -> R.style.AppTheme_Grey_300_core
            -4342339 -> R.style.AppTheme_Grey_400_core
            -6381922 -> R.style.AppTheme_Grey_500_core
            -9079435 -> R.style.AppTheme_Grey_600_core
            -10395295 -> R.style.AppTheme_Grey_700_core
            -12434878 -> R.style.AppTheme_Grey_800_core
            -16777216 -> R.style.AppTheme_Grey_900_core

            //-1 -> R.style.AppTheme_BW_10_core //-1 -> R.style.AppTheme_Grey_100_core
            -65794 -> R.style.AppTheme_BW_20_core  //add
            -1118482 -> R.style.AppTheme_BW_30_core  //add
            -2171170 -> R.style.AppTheme_BW_40_core  //add
            -3355444 -> R.style.AppTheme_BW_50_core
            -4671304 -> R.style.AppTheme_BW_60_core
            -5460820 -> R.style.AppTheme_BW_70_core  //add
            -6052957 -> R.style.AppTheme_BW_80_core
            -7368817 -> R.style.AppTheme_BW_90_core
            -8092540 -> R.style.AppTheme_BW_100_core  //add
            -8750470 -> R.style.AppTheme_BW_200_core
            -10066330 -> R.style.AppTheme_BW_300_core
            -10855846 -> R.style.AppTheme_BW_400_core  //add
            -11382190 -> R.style.AppTheme_BW_500_core
            -12763843 -> R.style.AppTheme_BW_600_core
            -13487566 -> R.style.AppTheme_BW_700_core  //add
            -14079703 -> R.style.AppTheme_BW_800_core
            -15461356 -> R.style.AppTheme_BW_900_core
            //-16777216 -> R.style.AppTheme_BW_000_core //-16777216 -> R.style.AppTheme_Grey_900_core

            -16047054 -> R.style.AppTheme_I_Blue_100_core
            -14593687 -> R.style.AppTheme_I_Blue_200_core
            -12876375 -> R.style.AppTheme_I_Blue_300_core
            -11552791 -> R.style.AppTheme_I_Blue_400_core
            -10369543 -> R.style.AppTheme_I_Blue_500_core
            -9054982 -> R.style.AppTheme_I_Blue_600_core
            -7546118 -> R.style.AppTheme_I_Blue_700_core
            -5511684 -> R.style.AppTheme_I_Blue_800_core
            -4134148 -> R.style.AppTheme_I_Blue_900_core

            -16576726 -> R.style.AppTheme_I_Indigo_100_core
            -16245164 -> R.style.AppTheme_I_Indigo_200_core
            -15584635 -> R.style.AppTheme_I_Indigo_300_core
            -14725699 -> R.style.AppTheme_I_Indigo_400_core
            -14065419 -> R.style.AppTheme_I_Indigo_500_core
            -12354058 -> R.style.AppTheme_I_Indigo_600_core
            -10053897 -> R.style.AppTheme_I_Indigo_700_core
            -7491336 -> R.style.AppTheme_I_Indigo_800_core
            -5389319 -> R.style.AppTheme_I_Indigo_900_core

            -15793613 -> R.style.AppTheme_I_Deep_Purple_100_core
            -14548114 -> R.style.AppTheme_I_Deep_Purple_200_core
            -13498967 -> R.style.AppTheme_I_Deep_Purple_300_core
            -12838970 -> R.style.AppTheme_I_Deep_Purple_400_core
            -9285675 -> R.style.AppTheme_I_Deep_Purple_500_core
            -8164131 -> R.style.AppTheme_I_Deep_Purple_600_core
            -5928465 -> R.style.AppTheme_I_Deep_Purple_700_core
            -4542993 -> R.style.AppTheme_I_Deep_Purple_800_core
            -3226378 -> R.style.AppTheme_I_Deep_Purple_900_core

            -14613965 -> R.style.AppTheme_I_Purple_100_core
            -11990423 -> R.style.AppTheme_I_Purple_200_core
            -9300831 -> R.style.AppTheme_I_Purple_300_core
            -8053057 -> R.style.AppTheme_I_Purple_400_core
            -6010162 -> R.style.AppTheme_I_Purple_500_core
            -5347881 -> R.style.AppTheme_I_Purple_600_core
            -4094239 -> R.style.AppTheme_I_Purple_700_core
            -2774803 -> R.style.AppTheme_I_Purple_800_core
            -1850379 -> R.style.AppTheme_I_Purple_900_core

            -14153453 -> R.style.AppTheme_I_Pink_100_core
            -11332825 -> R.style.AppTheme_I_Pink_200_core
            -8249282 -> R.style.AppTheme_I_Pink_300_core
            -4968872 -> R.style.AppTheme_I_Pink_400_core
            -4042634 -> R.style.AppTheme_I_Pink_500_core
            -3380080 -> R.style.AppTheme_I_Pink_600_core
            -2585944 -> R.style.AppTheme_I_Pink_700_core
            -1790526 -> R.style.AppTheme_I_Pink_800_core
            -1063466 -> R.style.AppTheme_I_Pink_900_core

            -14482175 -> R.style.AppTheme_I_Red_100_core
            -11989499 -> R.style.AppTheme_I_Red_200_core
            -9299700 -> R.style.AppTheme_I_Red_300_core
            -6281451 -> R.style.AppTheme_I_Red_400_core
            -2670558 -> R.style.AppTheme_I_Red_500_core
            -1355214 -> R.style.AppTheme_I_Red_600_core
            -1282476 -> R.style.AppTheme_I_Red_700_core
            -1076607 -> R.style.AppTheme_I_Red_800_core
            -938843 -> R.style.AppTheme_I_Red_900_core

            -14348798 -> R.style.AppTheme_I_Deep_Orange_100_core
            -11723257 -> R.style.AppTheme_I_Deep_Orange_200_core
            -8901104 -> R.style.AppTheme_I_Deep_Orange_300_core
            -5683684 -> R.style.AppTheme_I_Deep_Orange_400_core
            -1215698 -> R.style.AppTheme_I_Deep_Orange_500_core
            -1212867 -> R.style.AppTheme_I_Deep_Orange_600_core
            -1010337 -> R.style.AppTheme_I_Deep_Orange_700_core
            -806773 -> R.style.AppTheme_I_Deep_Orange_800_core
            -670807 -> R.style.AppTheme_I_Deep_Orange_900_core

            -14149372 -> R.style.AppTheme_I_Orange_100_core
            -11390196 -> R.style.AppTheme_I_Orange_200_core
            -8367591 -> R.style.AppTheme_I_Orange_300_core
            -4884183 -> R.style.AppTheme_I_Orange_400_core
            -1531603 -> R.style.AppTheme_I_Orange_500_core
            -872117 -> R.style.AppTheme_I_Orange_600_core
            -671640 -> R.style.AppTheme_I_Orange_700_core
            -536433 -> R.style.AppTheme_I_Orange_800_core
            -402515 -> R.style.AppTheme_I_Orange_900_core

            -14148348 -> R.style.AppTheme_I_Amber_100_core
            -11321840 -> R.style.AppTheme_I_Amber_200_core
            -8232162 -> R.style.AppTheme_I_Amber_300_core
            -4616145 -> R.style.AppTheme_I_Amber_400_core
            -867793 -> R.style.AppTheme_I_Amber_500_core
            -668590 -> R.style.AppTheme_I_Amber_600_core
            -535442 -> R.style.AppTheme_I_Amber_700_core
            -401261 -> R.style.AppTheme_I_Amber_800_core
            -334160 -> R.style.AppTheme_I_Amber_900_core

            -14540795 -> R.style.AppTheme_I_Yellow_100_core
            -12238321 -> R.style.AppTheme_I_Yellow_200_core
            -9804516 -> R.style.AppTheme_I_Yellow_300_core
            -7173077 -> R.style.AppTheme_I_Yellow_400_core
            -3620565 -> R.style.AppTheme_I_Yellow_500_core
            -1581262 -> R.style.AppTheme_I_Yellow_600_core
            -199342 -> R.style.AppTheme_I_Yellow_700_core
            -264571 -> R.style.AppTheme_I_Yellow_800_core
            -66907 -> R.style.AppTheme_I_Yellow_900_core

            -14605302 -> R.style.AppTheme_I_Lime_100_core
            -12302314 -> R.style.AppTheme_I_Lime_200_core
            -9867228 -> R.style.AppTheme_I_Lime_300_core
            -7235277 -> R.style.AppTheme_I_Lime_400_core
            -4667581 -> R.style.AppTheme_I_Lime_500_core
            -2300073 -> R.style.AppTheme_I_Lime_600_core
            -1511310 -> R.style.AppTheme_I_Lime_700_core
            -1051497 -> R.style.AppTheme_I_Lime_800_core
            -591181 -> R.style.AppTheme_I_Lime_900_core

            -14734572 -> R.style.AppTheme_I_Green_100_core
            -13285090 -> R.style.AppTheme_I_Green_200_core
            -11373777 -> R.style.AppTheme_I_Green_300_core
            -9331137 -> R.style.AppTheme_I_Green_400_core
            -7750314 -> R.style.AppTheme_I_Green_500_core
            -6305425 -> R.style.AppTheme_I_Green_600_core
            -4860274 -> R.style.AppTheme_I_Green_700_core
            -3349330 -> R.style.AppTheme_I_Green_800_core
            -2297915 -> R.style.AppTheme_I_Green_900_core

            else -> R.style.AppTheme_Blue_600_core //TODO DEFAULT THEME
        }
    }

    else -> {
        when (color) {
            -12846 -> R.style.AppTheme_Red_100
            -1074534 -> R.style.AppTheme_Red_200
            -1739917 -> R.style.AppTheme_Red_300
            -1092784 -> R.style.AppTheme_Red_400
            -769226 -> R.style.AppTheme_Red_500
            -1754827 -> R.style.AppTheme_Red_600
            -2937041 -> R.style.AppTheme_Red_700
            -3790808 -> R.style.AppTheme_Red_800
            -4776932 -> R.style.AppTheme_Red_900

            -476208 -> R.style.AppTheme_Pink_100
            -749647 -> R.style.AppTheme_Pink_200
            -1023342 -> R.style.AppTheme_Pink_300
            -1294214 -> R.style.AppTheme_Pink_400
            -1499549 -> R.style.AppTheme_Pink_500
            -2614432 -> R.style.AppTheme_Pink_600
            -4056997 -> R.style.AppTheme_Pink_700
            -5434281 -> R.style.AppTheme_Pink_800
            -7860657 -> R.style.AppTheme_Pink_900

            -1982745 -> R.style.AppTheme_Purple_100
            -3238952 -> R.style.AppTheme_Purple_200
            -4560696 -> R.style.AppTheme_Purple_300
            -5552196 -> R.style.AppTheme_Purple_400
            -6543440 -> R.style.AppTheme_Purple_500
            -7461718 -> R.style.AppTheme_Purple_600
            -8708190 -> R.style.AppTheme_Purple_700
            -9823334 -> R.style.AppTheme_Purple_800
            -11922292 -> R.style.AppTheme_Purple_900

            -3029783 -> R.style.AppTheme_Deep_Purple_100
            -5005861 -> R.style.AppTheme_Deep_Purple_200
            -6982195 -> R.style.AppTheme_Deep_Purple_300
            -8497214 -> R.style.AppTheme_Deep_Purple_400
            -10011977 -> R.style.AppTheme_Deep_Purple_500
            -10603087 -> R.style.AppTheme_Deep_Purple_600
            -11457112 -> R.style.AppTheme_Deep_Purple_700
            -12245088 -> R.style.AppTheme_Deep_Purple_800
            -13558894 -> R.style.AppTheme_Deep_Purple_900

            -3814679 -> R.style.AppTheme_Indigo_100
            -6313766 -> R.style.AppTheme_Indigo_200
            -8812853 -> R.style.AppTheme_Indigo_300
            -10720320 -> R.style.AppTheme_Indigo_400
            -12627531 -> R.style.AppTheme_Indigo_500
            -13022805 -> R.style.AppTheme_Indigo_600
            -13615201 -> R.style.AppTheme_Indigo_700
            -14142061 -> R.style.AppTheme_Indigo_800
            -15064194 -> R.style.AppTheme_Indigo_900

            -4464901 -> R.style.AppTheme_Blue_100
            -7288071 -> R.style.AppTheme_Blue_200
            -10177034 -> R.style.AppTheme_Blue_300
            -12409355 -> R.style.AppTheme_Blue_400
            -14575885 -> R.style.AppTheme_Blue_500
            -14776091 -> R.style.AppTheme_Blue_600
            -15108398 -> R.style.AppTheme_Blue_700
            -15374912 -> R.style.AppTheme_Blue_800
            -15906911 -> R.style.AppTheme_Blue_900

            -4987396 -> R.style.AppTheme_Light_Blue_100
            -8268550 -> R.style.AppTheme_Light_Blue_200
            -11549705 -> R.style.AppTheme_Light_Blue_300
            -14043396 -> R.style.AppTheme_Light_Blue_400
            -16537100 -> R.style.AppTheme_Light_Blue_500
            -16540699 -> R.style.AppTheme_Light_Blue_600
            -16611119 -> R.style.AppTheme_Light_Blue_700
            -16615491 -> R.style.AppTheme_Light_Blue_800
            -16689253 -> R.style.AppTheme_Light_Blue_900

            -5051406 -> R.style.AppTheme_Cyan_100
            -8331542 -> R.style.AppTheme_Cyan_200
            -11677471 -> R.style.AppTheme_Cyan_300
            -14235942 -> R.style.AppTheme_Cyan_400
            -16728876 -> R.style.AppTheme_Cyan_500
            -16732991 -> R.style.AppTheme_Cyan_600
            -16738393 -> R.style.AppTheme_Cyan_700
            -16743537 -> R.style.AppTheme_Cyan_800
            -16752540 -> R.style.AppTheme_Cyan_900

            -5054501 -> R.style.AppTheme_Teal_100
            -8336444 -> R.style.AppTheme_Teal_200
            -11684180 -> R.style.AppTheme_Teal_300
            -14244198 -> R.style.AppTheme_Teal_400
            -16738680 -> R.style.AppTheme_Teal_500
            -16742021 -> R.style.AppTheme_Teal_600
            -16746133 -> R.style.AppTheme_Teal_700
            -16750244 -> R.style.AppTheme_Teal_800
            -16757440 -> R.style.AppTheme_Teal_900

            -3610935 -> R.style.AppTheme_Green_100
            -5908825 -> R.style.AppTheme_Green_200
            -8271996 -> R.style.AppTheme_Green_300
            -10044566 -> R.style.AppTheme_Green_400
            -11751600 -> R.style.AppTheme_Green_500
            -12345273 -> R.style.AppTheme_Green_600
            -13070788 -> R.style.AppTheme_Green_700
            -13730510 -> R.style.AppTheme_Green_800
            -14983648 -> R.style.AppTheme_Green_900

            -2298424 -> R.style.AppTheme_Light_Green_100
            -3808859 -> R.style.AppTheme_Light_Green_200
            -5319295 -> R.style.AppTheme_Light_Green_300
            -6501275 -> R.style.AppTheme_Light_Green_400
            -7617718 -> R.style.AppTheme_Light_Green_500
            -8604862 -> R.style.AppTheme_Light_Green_600
            -9920712 -> R.style.AppTheme_Light_Green_700
            -11171025 -> R.style.AppTheme_Light_Green_800
            -13407970 -> R.style.AppTheme_Light_Green_900

            -985917 -> R.style.AppTheme_Lime_100
            -1642852 -> R.style.AppTheme_Lime_200
            -2300043 -> R.style.AppTheme_Lime_300
            -2825897 -> R.style.AppTheme_Lime_400
            -3285959 -> R.style.AppTheme_Lime_500
            -4142541 -> R.style.AppTheme_Lime_600
            -5983189 -> R.style.AppTheme_Lime_700
            -6382300 -> R.style.AppTheme_Lime_800
            -8227049 -> R.style.AppTheme_Lime_900

            -1596 -> R.style.AppTheme_Yellow_100
            -2672 -> R.style.AppTheme_Yellow_200
            -3722 -> R.style.AppTheme_Yellow_300
            -4520 -> R.style.AppTheme_Yellow_400
            -5317 -> R.style.AppTheme_Yellow_500
            -141259 -> R.style.AppTheme_Yellow_600
            -278483 -> R.style.AppTheme_Yellow_700
            -415707 -> R.style.AppTheme_Yellow_800
            -688361 -> R.style.AppTheme_Yellow_900

            -4941 -> R.style.AppTheme_Amber_100
            -8062 -> R.style.AppTheme_Amber_200
            -10929 -> R.style.AppTheme_Amber_300
            -13784 -> R.style.AppTheme_Amber_400
            -16121 -> R.style.AppTheme_Amber_500
            -19712 -> R.style.AppTheme_Amber_600
            -24576 -> R.style.AppTheme_Amber_700
            -28928 -> R.style.AppTheme_Amber_800
            -37120 -> R.style.AppTheme_Amber_900

            -8014 -> R.style.AppTheme_Orange_100
            -13184 -> R.style.AppTheme_Orange_200
            -18611 -> R.style.AppTheme_Orange_300
            -22746 -> R.style.AppTheme_Orange_400
            -26624 -> R.style.AppTheme_Orange_500
            -291840 -> R.style.AppTheme_Orange_600
            -689152 -> R.style.AppTheme_Orange_700
            -1086464 -> R.style.AppTheme_Orange_800
            -1683200 -> R.style.AppTheme_Orange_900

            -13124 -> R.style.AppTheme_Deep_Orange_100
            -21615 -> R.style.AppTheme_Deep_Orange_200
            -30107 -> R.style.AppTheme_Deep_Orange_300
            -36797 -> R.style.AppTheme_Deep_Orange_400
            -43230 -> R.style.AppTheme_Deep_Orange_500
            -765666 -> R.style.AppTheme_Deep_Orange_600
            -1684967 -> R.style.AppTheme_Deep_Orange_700
            -2604267 -> R.style.AppTheme_Deep_Orange_800
            -4246004 -> R.style.AppTheme_Deep_Orange_900

            -2634552 -> R.style.AppTheme_Brown_100
            -4412764 -> R.style.AppTheme_Brown_200
            -6190977 -> R.style.AppTheme_Brown_300
            -7508381 -> R.style.AppTheme_Brown_400
            -8825528 -> R.style.AppTheme_Brown_500
            -9614271 -> R.style.AppTheme_Brown_600
            -10665929 -> R.style.AppTheme_Brown_700
            -11652050 -> R.style.AppTheme_Brown_800
            -12703965 -> R.style.AppTheme_Brown_900

            -3155748 -> R.style.AppTheme_Blue_Grey_100
            -5194811 -> R.style.AppTheme_Blue_Grey_200
            -7297874 -> R.style.AppTheme_Blue_Grey_300
            -8875876 -> R.style.AppTheme_Blue_Grey_400
            -10453621 -> R.style.AppTheme_Blue_Grey_500
            -11243910 -> R.style.AppTheme_Blue_Grey_600
            -12232092 -> R.style.AppTheme_Blue_Grey_700
            -13154481 -> R.style.AppTheme_Blue_Grey_800
            -14273992 -> R.style.AppTheme_Blue_Grey_900

            -1 -> R.style.AppTheme_Grey_100 //TODO overflowIcon color for light theme
            //-1118482 -> R.style.AppTheme_Grey_200
            -2039584 -> R.style.AppTheme_Grey_300
            -4342339 -> R.style.AppTheme_Grey_400
            -6381922 -> R.style.AppTheme_Grey_500
            -9079435 -> R.style.AppTheme_Grey_600
            -10395295 -> R.style.AppTheme_Grey_700
            -12434878 -> R.style.AppTheme_Grey_800
            -16777216 -> R.style.AppTheme_Grey_900

            //-1 -> R.style.AppTheme_BW_10 //-1 -> R.style.AppTheme_Grey_100
            -65794 -> R.style.AppTheme_BW_20  //add
            -1118482 -> R.style.AppTheme_BW_30  //add
            -2171170 -> R.style.AppTheme_BW_40  //add
            -3355444 -> R.style.AppTheme_BW_50
            -4671304 -> R.style.AppTheme_BW_60
            -5460820 -> R.style.AppTheme_BW_70  //add
            -6052957 -> R.style.AppTheme_BW_80
            -7368817 -> R.style.AppTheme_BW_90
            -8092540 -> R.style.AppTheme_BW_100  //add
            -8750470 -> R.style.AppTheme_BW_200
            -10066330 -> R.style.AppTheme_BW_300
            -10855846 -> R.style.AppTheme_BW_400  //add
            -11382190 -> R.style.AppTheme_BW_500
            -12763843 -> R.style.AppTheme_BW_600
            -13487566 -> R.style.AppTheme_BW_700  //add
            -14079703 -> R.style.AppTheme_BW_800
            -15461356 -> R.style.AppTheme_BW_900
            //-16777216 -> R.style.AppTheme_BW_000 //-16777216 -> R.style.AppTheme_Grey_900

            -16047054 -> R.style.AppTheme_I_Blue_100
            -14593687 -> R.style.AppTheme_I_Blue_200
            -12876375 -> R.style.AppTheme_I_Blue_300
            -11552791 -> R.style.AppTheme_I_Blue_400
            -10369543 -> R.style.AppTheme_I_Blue_500
            -9054982 -> R.style.AppTheme_I_Blue_600
            -7546118 -> R.style.AppTheme_I_Blue_700
            -5511684 -> R.style.AppTheme_I_Blue_800
            -4134148 -> R.style.AppTheme_I_Blue_900

            -16576726 -> R.style.AppTheme_I_Indigo_100
            -16245164 -> R.style.AppTheme_I_Indigo_200
            -15584635 -> R.style.AppTheme_I_Indigo_300
            -14725699 -> R.style.AppTheme_I_Indigo_400
            -14065419 -> R.style.AppTheme_I_Indigo_500
            -12354058 -> R.style.AppTheme_I_Indigo_600
            -10053897 -> R.style.AppTheme_I_Indigo_700
            -7491336 -> R.style.AppTheme_I_Indigo_800
            -5389319 -> R.style.AppTheme_I_Indigo_900

            -15793613 -> R.style.AppTheme_I_Deep_Purple_100
            -14548114 -> R.style.AppTheme_I_Deep_Purple_200
            -13498967 -> R.style.AppTheme_I_Deep_Purple_300
            -12838970 -> R.style.AppTheme_I_Deep_Purple_400
            -9285675 -> R.style.AppTheme_I_Deep_Purple_500
            -8164131 -> R.style.AppTheme_I_Deep_Purple_600
            -5928465 -> R.style.AppTheme_I_Deep_Purple_700
            -4542993 -> R.style.AppTheme_I_Deep_Purple_800
            -3226378 -> R.style.AppTheme_I_Deep_Purple_900

            -14613965 -> R.style.AppTheme_I_Purple_100
            -11990423 -> R.style.AppTheme_I_Purple_200
            -9300831 -> R.style.AppTheme_I_Purple_300
            -8053057 -> R.style.AppTheme_I_Purple_400
            -6010162 -> R.style.AppTheme_I_Purple_500
            -5347881 -> R.style.AppTheme_I_Purple_600
            -4094239 -> R.style.AppTheme_I_Purple_700
            -2774803 -> R.style.AppTheme_I_Purple_800
            -1850379 -> R.style.AppTheme_I_Purple_900

            -14153453 -> R.style.AppTheme_I_Pink_100
            -11332825 -> R.style.AppTheme_I_Pink_200
            -8249282 -> R.style.AppTheme_I_Pink_300
            -4968872 -> R.style.AppTheme_I_Pink_400
            -4042634 -> R.style.AppTheme_I_Pink_500
            -3380080 -> R.style.AppTheme_I_Pink_600
            -2585944 -> R.style.AppTheme_I_Pink_700
            -1790526 -> R.style.AppTheme_I_Pink_800
            -1063466 -> R.style.AppTheme_I_Pink_900

            -14482175 -> R.style.AppTheme_I_Red_100
            -11989499 -> R.style.AppTheme_I_Red_200
            -9299700 -> R.style.AppTheme_I_Red_300
            -6281451 -> R.style.AppTheme_I_Red_400
            -2670558 -> R.style.AppTheme_I_Red_500
            -1355214 -> R.style.AppTheme_I_Red_600
            -1282476 -> R.style.AppTheme_I_Red_700
            -1076607 -> R.style.AppTheme_I_Red_800
            -938843 -> R.style.AppTheme_I_Red_900

            -14348798 -> R.style.AppTheme_I_Deep_Orange_100
            -11723257 -> R.style.AppTheme_I_Deep_Orange_200
            -8901104 -> R.style.AppTheme_I_Deep_Orange_300
            -5683684 -> R.style.AppTheme_I_Deep_Orange_400
            -1215698 -> R.style.AppTheme_I_Deep_Orange_500
            -1212867 -> R.style.AppTheme_I_Deep_Orange_600
            -1010337 -> R.style.AppTheme_I_Deep_Orange_700
            -806773 -> R.style.AppTheme_I_Deep_Orange_800
            -670807 -> R.style.AppTheme_I_Deep_Orange_900

            -14149372 -> R.style.AppTheme_I_Orange_100
            -11390196 -> R.style.AppTheme_I_Orange_200
            -8367591 -> R.style.AppTheme_I_Orange_300
            -4884183 -> R.style.AppTheme_I_Orange_400
            -1531603 -> R.style.AppTheme_I_Orange_500
            -872117 -> R.style.AppTheme_I_Orange_600
            -671640 -> R.style.AppTheme_I_Orange_700
            -536433 -> R.style.AppTheme_I_Orange_800
            -402515 -> R.style.AppTheme_I_Orange_900

            -14148348 -> R.style.AppTheme_I_Amber_100
            -11321840 -> R.style.AppTheme_I_Amber_200
            -8232162 -> R.style.AppTheme_I_Amber_300
            -4616145 -> R.style.AppTheme_I_Amber_400
            -867793 -> R.style.AppTheme_I_Amber_500
            -668590 -> R.style.AppTheme_I_Amber_600
            -535442 -> R.style.AppTheme_I_Amber_700
            -401261 -> R.style.AppTheme_I_Amber_800
            -334160 -> R.style.AppTheme_I_Amber_900

            -14540795 -> R.style.AppTheme_I_Yellow_100
            -12238321 -> R.style.AppTheme_I_Yellow_200
            -9804516 -> R.style.AppTheme_I_Yellow_300
            -7173077 -> R.style.AppTheme_I_Yellow_400
            -3620565 -> R.style.AppTheme_I_Yellow_500
            -1581262 -> R.style.AppTheme_I_Yellow_600
            -199342 -> R.style.AppTheme_I_Yellow_700
            -264571 -> R.style.AppTheme_I_Yellow_800
            -66907 -> R.style.AppTheme_I_Yellow_900

            -14605302 -> R.style.AppTheme_I_Lime_100
            -12302314 -> R.style.AppTheme_I_Lime_200
            -9867228 -> R.style.AppTheme_I_Lime_300
            -7235277 -> R.style.AppTheme_I_Lime_400
            -4667581 -> R.style.AppTheme_I_Lime_500
            -2300073 -> R.style.AppTheme_I_Lime_600
            -1511310 -> R.style.AppTheme_I_Lime_700
            -1051497 -> R.style.AppTheme_I_Lime_800
            -591181 -> R.style.AppTheme_I_Lime_900

            -14734572 -> R.style.AppTheme_I_Green_100
            -13285090 -> R.style.AppTheme_I_Green_200
            -11373777 -> R.style.AppTheme_I_Green_300
            -9331137 -> R.style.AppTheme_I_Green_400
            -7750314 -> R.style.AppTheme_I_Green_500
            -6305425 -> R.style.AppTheme_I_Green_600
            -4860274 -> R.style.AppTheme_I_Green_700
            -3349330 -> R.style.AppTheme_I_Green_800
            -2297915 -> R.style.AppTheme_I_Green_900

            else -> R.style.AppTheme_Blue_600 //TODO DEFAULT THEME
        }
    }
}
