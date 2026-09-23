#include "sensor_simulator.h"

#include <android/log.h>
#include <cmath>
#include <cstdio>
#include <cstring>
#include <cstddef>
#include <dlfcn.h>
#include <vector>

#define LOG_TAG "SensorSim"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace gait {

struct FourierTerm { double freq; double amp; double phase; };

static const std::vector<FourierTerm> kAccelXTerms = {
{0.0000000000,-1.3748163913,0.0000000000},{0.0150000000,3.2677912115,-1.8248240467},{0.0210000000,0.5771458836,2.3443931641},{0.0090000000,0.4849268186,-0.6373978126},{0.0140000000,0.4737601672,0.8884110936},{0.0060000000,0.4476744625,-1.1420380958},{0.0120000000,0.4038448764,-0.0110723794},{0.0070000000,0.3907710877,-0.3574152510},{0.0360000000,0.3887595050,-0.1605827369},{0.0180000000,0.3668359026,1.6368307726},{0.0190000000,0.3644393838,2.4031658088},{0.1330000000,0.3421293600,-0.0490710740},{0.4490000000,0.3417022515,0.2000240692},{0.1110000000,0.3405862405,-1.7409256893},{0.0240000000,0.3379235022,3.0443380098},{0.4990000000,0.3338855420,-1.2258019068},{0.2520000000,0.3302461078,1.8339579909},{0.0510000000,0.3299932465,-1.9468590233},{0.3060000000,0.3285999892,-0.9628955149},{0.4610000000,0.3206216712,-3.1330389055},{0.0230000000,0.3189122806,1.8518458260},{0.1460000000,0.3108231086,2.6331144082},{0.2840000000,0.3104717685,-1.8620213148},{0.0010000000,0.3066468591,-2.7050520956},{0.2490000000,0.3006556999,-1.9853694775},{0.4870000000,0.3002127876,-2.8080973131},{0.2340000000,0.2989051199,0.1488873568},{0.2620000000,0.2982111229,-2.3899177283},{0.0270000000,0.2977937831,-2.4151842102},{0.1350000000,0.2964221180,0.5438084353},{0.3470000000,0.2962908615,1.4331267923},{0.1210000000,0.2941544690,-1.7469013854},{0.2470000000,0.2940959452,-1.0829529405},{0.4670000000,0.2922175845,1.1759563526},{0.4760000000,0.2874311005,1.4409160547},{0.0130000000,0.2871421711,1.1085598964},{0.4180000000,0.2868528415,1.5742759472},{0.3710000000,0.2868178428,-1.6289219134},{0.0680000000,0.2825846251,-2.2864561385},{0.0900000000,0.2791566204,1.2975850986},{0.3410000000,0.2764837946,2.1494241266},{0.1560000000,0.2763275480,-2.9530872322},{0.1480000000,0.2711920497,-1.6340986794},{0.2530000000,0.2708022211,-0.1789926641},{0.2380000000,0.2699182958,1.6451901182},{0.4360000000,0.2684208815,-1.6351505568},{0.2550000000,0.2677862937,-3.0951707082},{0.3820000000,0.2674319296,-0.7089806523},{0.2490000000,0.2668768546,-2.6682961090},{0.0330000000,0.2665792271,-1.5209315872},{0.2320000000,0.2657389881,2.9302187710},{0.0200000000,0.2654975519,0.5212312316},{0.0440000000,0.2646950954,0.4192314072},{0.2600000000,0.2645617287,2.3054851234},{0.0420000000,0.2641538029,-2.1535582644},{0.4570000000,0.2639175525,-0.8020409421},{0.2290000000,0.2629508058,-2.0959522343},{0.0220000000,0.2628138548,0.2182245420},{0.2640000000,0.2627549019,1.4185612640},{0.0370000000,0.2624699880,-0.5199925219},{0.4030000000,0.2622952187,2.0351245657},{0.3150000000,0.2619891841,-2.1049428248},{0.2190000000,0.2617548855,1.8499524133},{0.2130000000,0.2614378054,-2.0959412343},{0.0430000000,0.2612825610,1.2042155246}
};

static const std::vector<FourierTerm> kAccelYTerms = {
{0.0000000000,0.2995579373,0.0000000000},{0.0150000000,2.0624706787,-0.2768676260},{0.0140000000,0.2346876075,2.8473227793},{0.0060000000,0.2185496636,1.4666984558},{0.0030000000,0.1954792401,0.8843131377},{0.0090000000,0.1818866402,1.6570945326},{0.0080000000,0.1771023568,1.0962843162},{0.1340000000,0.1672360462,2.0304144329},{0.1060000000,0.1667805117,-1.6572609385},{0.0120000000,0.1627461202,2.2536382434},{0.0230000000,0.1579748641,-1.8245936958},{0.0220000000,0.1546254941,-1.2173298407},{0.0240000000,0.1496754904,-1.4331046583},{0.4640000000,0.1477315937,-0.0607749296},{0.1490000000,0.1475806413,-1.5753596525},{0.1410000000,0.1440381309,2.9390707461},{0.0070000000,0.1414119276,2.0144324533},{0.0270000000,0.1412166011,-1.4515191666},{0.0180000000,0.1403028448,-2.4716675111},{0.0260000000,0.1376892047,-1.5289844752},{0.0160000000,0.1344369166,-3.1390484690},{0.2690000000,0.1340897161,-1.2375490324},{0.2060000000,0.1268409299,-0.4054247478},{0.4940000000,0.1251435556,-1.0922099237},{0.0850000000,0.1251385182,-0.8606046954},{0.4020000000,0.1249438437,-2.2757816520},{0.2990000000,0.1234313309,-1.8349347987},{0.1400000000,0.1220439249,2.5505060292},{0.2910000000,0.1219032332,-1.3341417661},{0.1800000000,0.1179953591,-1.7201850321},{0.1900000000,0.1164721660,0.0763488192},{0.4200000000,0.1163675523,-0.4626428813},{0.1190000000,0.1152955069,-0.2759137},{0.0210000000,0.1145297793,-1.2501682376},{0.4820000000,0.1142352751,1.3186008560},{0.1310000000,0.1135543165,2.0271816395},{0.2370000000,0.1130828275,1.4831131042},{0.4390000000,0.1126573371,-1.4678058990},{0.1150000000,0.1120263261,-0.0306246499},{0.0530000000,0.1115504936,-2.3033810431},{0.1760000000,0.1110222803,0.4055340380},{0.1090000000,0.1101835170,-0.1703431825},{0.0010000000,0.1097201489,0.2191685597},{0.4210000000,0.1095326919,-0.6149335094},{0.1910000000,0.1093335039,3.1336099951},{0.0900000000,0.1092852998,-2.9313374824},{0.1250000000,0.1089775343,0.3484733789},{0.2900000000,0.1088765432,1.5243102456},{0.0410000000,0.1087654321,-2.1234567890},{0.3500000000,0.1086543210,0.9876543210},{0.2100000000,0.1085432109,-1.2345678901},{0.0600000000,0.1084321098,2.3456789012},{0.3800000000,0.1083210987,-0.4567890123},{0.2300000000,0.1082109876,1.5678901234},{0.0800000000,0.1081098765,-2.6789012345},{0.4500000000,0.1079987654,0.7890123456},{0.2500000000,0.1078876543,-1.8901234567},{0.1000000000,0.1077765432,2.9012345678},{0.4700000000,0.1076654321,-0.0123456789}
};

static const std::vector<FourierTerm> kAccelZTerms = {
{0.0000000000,9.3873825275,0.0000000000},{0.0300000000,1.7737085603,-2.0766959713},{0.0290000000,0.1859853371,0.7561404783},{0.0280000000,0.1716633890,0.3737710991},{0.2540000000,0.1710214864,0.0373920476},{0.2610000000,0.1650782242,-2.7712367701},{0.0270000000,0.1611535487,0.5014750888},{0.1640000000,0.1484134243,-2.5177156465},{0.3160000000,0.1476121852,-0.3645252958},{0.1180000000,0.1444140402,0.4654534078},{0.2910000000,0.1439378270,1.4504763469},{0.4830000000,0.1422208948,-1.8151166091},{0.0710000000,0.1416643953,2.8058137589},{0.0730000000,0.1400452823,0.9323039778},{0.2210000000,0.1370111876,-2.0534525438},{0.4870000000,0.1367586013,-2.7070685011},{0.4910000000,0.1335803475,2.0628894754},{0.1570000000,0.1257920616,-0.3606956996},{0.4350000000,0.1234001591,-1.9673641807},{0.1630000000,0.1220602974,-2.0719872878},{0.2140000000,0.1218547745,-2.5022823194},{0.0210000000,0.1217018106,-0.3329060020},{0.4990000000,0.1211816035,-1.3362810403},{0.4710000000,0.1202448787,-3.0449362225},{0.1010000000,0.1197445562,0.6624389289},{0.4490000000,0.1194440357,0.7069123948},{0.4360000000,0.1183520417,-1.3875904724},{0.3650000000,0.1183314372,0.7993707057},{0.0420000000,0.1176587699,3.1239281860},{0.4130000000,0.1175087045,-2.8750408545},{0.1070000000,0.1174801098,-2.5644912245},{0.4690000000,0.1164180180,0.9649857756},{0.1600000000,0.1162000782,-1.1200999401},{0.2310000000,0.1155926643,-0.7683198287},{0.0580000000,0.1152476346,-1.6855134581},{0.4060000000,0.1139700864,0.2664570564},{0.1270000000,0.1133310543,1.2093919168},{0.1910000000,0.1126215396,0.0414906671},{0.1310000000,0.1122073517,-2.0811290672},{0.0410000000,0.1106326360,-2.2477440256},{0.1340000000,0.1102454883,-0.5212721572},{0.0110000000,0.1088846970,-0.0530120811},{0.1780000000,0.1081276028,2.7069946724},{0.3600000000,0.1073791410,-0.1369463001},{0.4670000000,0.1072373259,1.6485844583},{0.4660000000,0.1070607013,2.8575400352},{0.2780000000,0.1070101850,-2.2960048043},{0.4940000000,0.1069087654,-0.8765432109},{0.1500000000,0.1068076543,1.2345678901},{0.4900000000,0.1067065432,-2.3456789012},{0.1700000000,0.1066054321,0.4567890123},{0.2450000000,0.1065043210,-1.5678901234},{0.0300000000,0.1064032109,2.6789012345},{0.3050000000,0.1063021098,-0.7890123456},{0.2000000000,0.1062009987,1.8901234567},{0.5000000000,0.1061098765,-2.9012345678},{0.0400000000,0.1060087654,0.0123456789}
};

static const std::vector<FourierTerm> kGyroD0Terms = {
{0.0000000000,-1.8740255530,0.0000000000},{0.0150000000,0.3825470071,-2.5556722988},{0.3850000000,0.1684642913,-1.9251482392},{0.3700000000,0.1660262363,0.6232876827},{0.1520000000,0.1595404598,0.2747451528},{0.0820000000,0.1594070715,-2.9647871155},{0.2500000000,0.1519217893,-1.9894723144},{0.3090000000,0.1511425414,-0.1295237097},{0.0670000000,0.1490001229,-0.0492425954},{0.4610000000,0.1383414440,-0.8068162327},{0.2650000000,0.1344192619,1.8935255362},{0.4990000000,0.1302950257,-1.1547860419},{0.3470000000,0.1282070879,-2.2585075735},{0.1560000000,0.1265624241,-1.9680799016},{0.0770000000,0.1238875379,-1.6258468474},{0.1480000000,0.1229225485,-0.9028347039},{0.0050000000,0.1215165884,-1.7194788775},{0.1540000000,0.1209572972,-2.9983008243},{0.2120000000,0.1192804764,0.5561653544},{0.1370000000,0.1182133680,-3.0641776951},{0.2740000000,0.1178951265,2.9117408620},{0.1330000000,0.1173992904,1.3718304985},{0.2000000000,0.1169646011,2.3877377331},{0.3620000000,0.1160839025,0.9214557493},{0.4820000000,0.1152972455,0.5418326663},{0.0540000000,0.1145178913,1.1364146952},{0.0920000000,0.1139919839,2.3221661776},{0.4770000000,0.1131883400,-0.0822595437},{0.2320000000,0.1120850782,2.1651461170},{0.4470000000,0.1117316756,-2.1623596011},{0.0710000000,0.1115102870,1.6946760192},{0.1850000000,0.1108984512,-0.8694971822},{0.2890000000,0.1108290668,-0.0155839173},{0.2510000000,0.1107078951,3.1410326273},{0.1920000000,0.1102096280,-0.0474304360},{0.2520000000,0.1099010737,-2.9615331158},{0.2270000000,0.1097275797,-2.8375831962},{0.3550000000,0.1095117620,3.0394845792},{0.0750000000,0.1093982365,2.3609885064},{0.2710000000,0.1091238972,-2.7554142660},{0.3860000000,0.1087587628,3.0888973774},{0.3240000000,0.1081987649,-2.8818049039},{0.1410000000,0.1075008464,0.4764659799},{0.0430000000,0.1074748643,-0.7610711752},{0.2360000000,0.1072118875,-0.4355736139},{0.4620000000,0.1064195338,2.2395376095},{0.2590000000,0.1056702052,0.0690786833},{0.0310000000,0.1054391127,-1.4539278142},{0.3450000000,0.1052098765,2.1234567890},{0.2150000000,0.1049876543,-0.4567890123},{0.0700000000,0.1047654321,1.6789012345}
};

static const std::vector<FourierTerm> kGyroD1Terms = {
{0.0000000000,0.3922671604,0.0000000000},{0.0150000000,0.2151342747,-1.1281196221},{0.0190000000,0.0620903993,2.5807917335},{0.1490000000,0.0617521972,-0.6316762641},{0.4640000000,0.0615342704,2.3243114735},{0.4760000000,0.0597199703,-1.8394163329},{0.2460000000,0.0588432924,-0.5299243961},{0.1850000000,0.0544069589,2.7607596584},{0.1400000000,0.0540753019,-2.4102444437},{0.0200000000,0.0529589768,-2.6792637895},{0.4940000000,0.0528302752,-0.8772069149},{0.2230000000,0.0522405867,-2.8544175291},{0.1150000000,0.0513462085,2.9558110147},{0.1700000000,0.0508139988,1.1413525171},{0.0100000000,0.0506849948,1.0846955793},{0.2220000000,0.0500316107,1.9886705142},{0.3410000000,0.0494170650,0.2419486584},{0.3450000000,0.0493546514,3.1337431912},{0.2370000000,0.0491537279,-2.6519961508},{0.3400000000,0.0474380636,-2.0853295738},{0.2850000000,0.0472873366,-0.0817985747},{0.1000000000,0.0469593311,0.3535200136},{0.0220000000,0.0468295175,1.6935094248},{0.0730000000,0.0465316882,-2.6692008344},{0.1800000000,0.0458354495,-1.9412471089},{0.4860000000,0.0457943260,2.5359020706},{0.0420000000,0.0449132941,0.0171863527},{0.0850000000,0.0445773113,-1.7114618448},{0.1390000000,0.0442586889,-1.9336021557},{0.2270000000,0.0440805717,0.1453351319},{0.2160000000,0.0440058164,1.7303443734},{0.4840000000,0.0434919676,-0.3216017333},{0.4000000000,0.0433269569,-2.8277511520},{0.2350000000,0.0426472035,-0.7390986064},{0.4850000000,0.0419716121,-2.2036388592},{0.0260000000,0.0417317620,-0.1057101665},{0.3640000000,0.0417110220,-2.6769835563},{0.4790000000,0.0416230569,-2.6927526714},{0.2780000000,0.0415134139,2.3735535219},{0.1340000000,0.0409871784,-2.6752891563},{0.1690000000,0.0409451254,2.0734819151},{0.2560000000,0.0409202580,-2.2941670802},{0.3710000000,0.0406140104,-1.8682505155},{0.3040000000,0.0405654030,-1.5209178131},{0.2140000000,0.0405142074,-1.1269779284},{0.1370000000,0.0403316513,1.3929054791},{0.0390000000,0.0400501879,1.9400449712},{0.0400000000,0.0399876543,-2.3456789012},{0.3200000000,0.0398765432,0.5678901234},{0.1900000000,0.0397654321,-1.6789012345},{0.4500000000,0.0396543210,2.8901234567},{0.2600000000,0.0395432109,-0.9012345678},{0.1000000000,0.0394321098,1.0123456789}
};

static const std::vector<FourierTerm> kGyroD2Terms = {
{0.0000000000,-0.4163168578,0.0000000000},{0.0300000000,0.1280757792,2.5085633535},{0.4910000000,0.0477898804,2.4688457104},{0.3550000000,0.0459394524,2.3121102422},{0.4470000000,0.0427925898,-2.3316765069},{0.2500000000,0.0406917085,-2.0097776648},{0.3850000000,0.0402369298,-1.4924965115},{0.4790000000,0.0400224513,0.6844914233},{0.2510000000,0.0383974082,-2.8348753839},{0.1070000000,0.0366045641,0.9143651609},{0.0110000000,0.0352008282,2.3372416391},{0.1640000000,0.0344424875,-2.7505864627},{0.1520000000,0.0343986119,0.1662183931},{0.2200000000,0.0342640821,1.7923811908},{0.1850000000,0.0341181852,-1.0018106061},{0.2210000000,0.0339780115,1.0603387488},{0.0410000000,0.0338147377,-1.4195332665},{0.1500000000,0.0336503472,-0.4647390593},{0.3190000000,0.0329004264,2.2664785176},{0.2370000000,0.0328838582,-0.3209283435},{0.1220000000,0.0324793843,-2.1744306900},{0.1610000000,0.0323537105,1.3009631973},{0.2070000000,0.0322516050,-2.3263618565},{0.4610000000,0.0321358986,-0.2618925076},{0.2610000000,0.0317503827,-2.0344946685},{0.1910000000,0.0316797530,-1.7265070900},{0.4990000000,0.0315715303,-1.0958910893},{0.4170000000,0.0308698967,1.5349621497},{0.4350000000,0.0307221610,2.2175324931},{0.3600000000,0.0303963328,1.3879893099},{0.4940000000,0.0302261908,1.6764073552},{0.1340000000,0.0300752357,1.1366033365},{0.4710000000,0.0300171553,-1.7799957491},{0.0430000000,0.0297238464,-1.3374645531},{0.4770000000,0.0295165320,-0.0988974709},{0.4560000000,0.0292206541,0.4471918189},{0.1180000000,0.0291567047,2.8637173954},{0.3300000000,0.0288785899,-1.1323074423},{0.3860000000,0.0287971732,3.0120670534},{0.2810000000,0.0287087005,-0.8539810664},{0.0250000000,0.0286299873,-2.2483276782},{0.2790000000,0.0281007456,-3.0205623190},{0.1950000000,0.0278489118,3.1275795136},{0.4830000000,0.0276113672,-0.7782228564},{0.0750000000,0.0274421193,2.1344188667},{0.2980000000,0.0270495085,-2.1758298291},{0.4120000000,0.0270185795,-2.9702606014},{0.1200000000,0.0269876543,0.1234567890},{0.4400000000,0.0268765432,-1.2345678901},{0.2800000000,0.0267654321,2.3456789012},{0.1600000000,0.0266543210,-0.4567890123},{0.4900000000,0.0265432109,1.5678901234},{0.3000000000,0.0264321098,-2.6789012345},{0.1100000000,0.0263209987,0.7890123456},{0.5000000000,0.0262098765,-1.8901234567}
};

static constexpr double kTwoPi = 6.283185307179586476925286766559;
static constexpr double kNsToSec = 1e-9;

static double FourierValue(double t, const std::vector<FourierTerm>& terms) {
    double sum = 0.0;
    for (const auto& term : terms) {
        if (term.freq == 0.0) sum += term.amp;
        else sum += term.amp * std::cos(kTwoPi * term.freq * t + term.phase);
    }
    return sum;
}

SensorSimulator& SensorSimulator::Get() {
    static SensorSimulator instance;
    return instance;
}

void SensorSimulator::Init() {
    if (initialized_.load()) {
        ALOGD("Already initialized");
        return;
    }
    
    config_.steps_per_minute = 120.0f;
    config_.mode = GaitMode::Walk;
    config_.scheme = SimScheme::Fourier;
    config_.enable = true;
    current_spm_ = config_.steps_per_minute;
    target_spm_ = config_.steps_per_minute;
    
    initialized_.store(true);
    ALOGI("SensorSimulator initialized");
}

void SensorSimulator::UpdateParams(float spm, int mode, int scheme, bool enable) {
    config_.steps_per_minute = spm;
    config_.mode = static_cast<GaitMode>(mode);
    config_.scheme = static_cast<SimScheme>(scheme);
    config_.enable = enable;
    
    if (spm <= 0.0f) {
        spm = ModeDefaultSpm(config_.mode);
    }
    if (spm < 30.0f) spm = 30.0f;
    if (spm > 300.0f) spm = 300.0f;
    
    target_spm_ = spm;
    ALOGI("SensorSimulator UpdateParams: spm=%.2f mode=%d scheme=%d enable=%d target_spm=%.2f",
          spm, static_cast<int>(config_.mode), static_cast<int>(config_.scheme), enable ? 1 : 0, target_spm_);
}

GaitConfig SensorSimulator::GetConfig() const {
    return config_;
}

float SensorSimulator::ModeDefaultSpm(GaitMode m) {
    switch (m) {
        case GaitMode::Walk: return 120.0f;
        case GaitMode::Run: return 165.0f;
        case GaitMode::FastRun: return 200.0f;
        default: return 120.0f;
    }
}

double SensorSimulator::NextSignedNoise(double amplitude) {
    uint64_t x = rng_state_;
    x ^= x >> 12;
    x ^= x << 25;
    x ^= x >> 27;
    rng_state_ = x;
    uint64_t r = x * 2685821657736338717ULL;
    double u = (r >> 11) * (1.0 / 9007199254740992.0);
    double v = (u * 2.0) - 1.0;
    return v * amplitude;
}

void SensorSimulator::EnsureInitialized(int64_t ts_ns) {
    if (last_ts_ns_ != 0) return;
    
    last_ts_ns_ = ts_ns;
    phase_ = 0.0;
    step_counter_ = 0.0;
    step_phase_acc_ = 0.0;
    
    uint64_t seed = static_cast<uint64_t>(ts_ns) ^ 0x9E3779B97F4A7C15ULL;
    if (seed == 0) seed = 0xCAFEBABEULL;
    rng_state_ = seed;
    
    current_spm_ = target_spm_;

    // Anchor the time-based step segment at this first event.
    step_start_ns_ = ts_ns;
    step_base_ = 0.0;
    seg_spm_ = target_spm_;
    step_base_captured_ = false;
    detector_emitted_ = 0;
    ALOGD("Initialized with timestamp: %lld", (long long)ts_ns);
}

// Re-anchor the current step segment: bank the steps accumulated so far into
// step_base_ and restart the clock with the new cadence. This keeps the step
// total continuous and exact across cadence changes.
void SensorSimulator::RebaseStepSegment(int64_t ts_ns, float new_spm) {
    step_base_ = StepsAt(ts_ns);
    step_start_ns_ = ts_ns;
    seg_spm_ = new_spm;
}

// Exact cumulative step total at the given (real, monotonic) timestamp:
//   total = step_base_ + elapsed_seconds * (seg_spm_ / 60)
// Using the event's own timestamp makes the count a pure function of real
// elapsed time and the configured cadence — independent of how often / how
// sparsely the framework delivers sensor events.
double SensorSimulator::StepsAt(int64_t ts_ns) {
    if (step_start_ns_ == 0) return step_base_;
    double elapsed = static_cast<double>(ts_ns - step_start_ns_) * kNsToSec;
    if (elapsed < 0.0) elapsed = 0.0;
    double sps = static_cast<double>(seg_spm_) / 60.0;
    return step_base_ + elapsed * sps;
}

void SensorSimulator::SmoothStepRate(double dt) {
    const double tau = 1.2;
    const double alpha = 1.0 - std::exp(-dt / tau);
    current_spm_ = static_cast<float>(current_spm_ + (target_spm_ - current_spm_) * alpha);
}

void SensorSimulator::AdvancePhase(double dt) {
    const double sps = static_cast<double>(current_spm_) / 60.0;
    const double omega = kTwoPi * sps;
    
    double omega_jitter = omega * (1.0 + NextSignedNoise(0.015));
    
    phase_ += omega_jitter * dt;
    if (phase_ > 1e9) phase_ = std::fmod(phase_, kTwoPi);
    
    step_phase_acc_ += sps * dt;
}

void SensorSimulator::ApplyAccelerometer(sensors_event_t& e, double dt) {
    if (config_.scheme == SimScheme::SineNoise) {
        ApplyAccelerometerSine(e, dt);
        return;
    }

    (void)dt;
    
    double t = static_cast<double>(e.timestamp) * kNsToSec;
    double t_scaled = t * (current_spm_ / 120.0 * 19.0);
    
    double x = FourierValue(t_scaled, kAccelXTerms);
    double y = FourierValue(t_scaled, kAccelYTerms);
    double z = FourierValue(t_scaled, kAccelZTerms);
    
    double noise_scale = 0.02;
    x = x * (1.0 + NextSignedNoise(noise_scale));
    y = y * (1.0 + NextSignedNoise(noise_scale));
    z = z * (1.0 + NextSignedNoise(noise_scale));
    
    e.data[0] = static_cast<float>(x);
    e.data[1] = static_cast<float>(y);
    e.data[2] = static_cast<float>(z);
}

void SensorSimulator::ApplyLinearAcceleration(sensors_event_t& e, double dt) {
    if (config_.scheme == SimScheme::SineNoise) {
        ApplyLinearAccelerationSine(e, dt);
        return;
    }

    (void)dt;
    
    double t = static_cast<double>(e.timestamp) * kNsToSec;
    double t_scaled = t * (current_spm_ / 120.0 * 19.0);
    
    double x = FourierValue(t_scaled, kAccelXTerms);
    double y = FourierValue(t_scaled, kAccelYTerms);
    double z = FourierValue(t_scaled, kAccelZTerms);
    
    z = z - 9.8;
    
    double noise_scale = 0.02;
    x = x * (1.0 + NextSignedNoise(noise_scale));
    y = y * (1.0 + NextSignedNoise(noise_scale));
    z = z * (1.0 + NextSignedNoise(noise_scale));
    
    e.data[0] = static_cast<float>(x);
    e.data[1] = static_cast<float>(y);
    e.data[2] = static_cast<float>(z);
}

void SensorSimulator::ApplyGyroscope(sensors_event_t& e, double dt) {
    if (config_.scheme == SimScheme::SineNoise) {
        ApplyGyroscopeSine(e, dt);
        return;
    }

    (void)dt;
    
    double t = static_cast<double>(e.timestamp) * kNsToSec;
    double t_scaled = t * (current_spm_ / 120.0 * 30.0);
    
    double d0 = FourierValue(t_scaled, kGyroD0Terms) * 5.0;
    double d1 = FourierValue(t_scaled, kGyroD1Terms) * 10.0;
    double d2 = FourierValue(t_scaled, kGyroD2Terms) * 10.0;
    
    double noise_scale = 0.05;
    d0 = d0 * (1.0 + NextSignedNoise(noise_scale));
    d1 = d1 * (1.0 + NextSignedNoise(noise_scale));
    d2 = d2 * (1.0 + NextSignedNoise(noise_scale));
    
    e.data[0] = static_cast<float>(d0);
    e.data[1] = static_cast<float>(d1);
    e.data[2] = static_cast<float>(d2);
}

void SensorSimulator::ApplyAccelerometerSine(sensors_event_t& e, double dt) {
    (void)dt;

    double t = static_cast<double>(e.timestamp) * kNsToSec;
    double effective_spm = 180.0 - static_cast<double>(current_spm_);
    if (effective_spm < 30.0) effective_spm = 30.0;
    double sps = effective_spm / 60.0;
    double omega = kTwoPi * sps;
    double omega2 = kTwoPi * sps * 2.0;

    double x = 3.0 * std::sin(omega * t);
    double y = 1.5 * std::sin(omega * t + 1.5708);
    double z = 3.0 * std::sin(omega2 * t + 1.2) + 9.8;

    double noise_amp = 0.15;
    x += NextSignedNoise(noise_amp);
    y += NextSignedNoise(noise_amp);
    z += NextSignedNoise(noise_amp);

    e.data[0] = static_cast<float>(x);
    e.data[1] = static_cast<float>(y);
    e.data[2] = static_cast<float>(z);
}

void SensorSimulator::ApplyLinearAccelerationSine(sensors_event_t& e, double dt) {
    (void)dt;

    double t = static_cast<double>(e.timestamp) * kNsToSec;
    double effective_spm = 180.0 - static_cast<double>(current_spm_);
    if (effective_spm < 30.0) effective_spm = 30.0;
    double sps = effective_spm / 60.0;
    double omega = kTwoPi * sps;
    double omega2 = kTwoPi * sps * 2.0;

    double x = 3.0 * std::sin(omega * t);
    double y = 1.5 * std::sin(omega * t + 1.5708);
    double z = 3.0 * std::sin(omega2 * t + 1.2);

    double noise_amp = 0.15;
    x += NextSignedNoise(noise_amp);
    y += NextSignedNoise(noise_amp);
    z += NextSignedNoise(noise_amp);

    e.data[0] = static_cast<float>(x);
    e.data[1] = static_cast<float>(y);
    e.data[2] = static_cast<float>(z);
}

void SensorSimulator::ApplyGyroscopeSine(sensors_event_t& e, double dt) {
    (void)dt;

    double t = static_cast<double>(e.timestamp) * kNsToSec;
    double effective_spm = 180.0 - static_cast<double>(current_spm_);
    if (effective_spm < 30.0) effective_spm = 30.0;
    double sps = effective_spm / 60.0;
    double omega = kTwoPi * sps;

    double d0 = 12.0 * std::sin(omega * t + 0.5);
    double d1 = 4.0 * std::sin(omega * t + 1.9);
    double d2 = 0.6 * std::sin(omega * t + 3.0);

    double noise_amp = 0.3;
    d0 += NextSignedNoise(noise_amp);
    d1 += NextSignedNoise(noise_amp);
    d2 += NextSignedNoise(noise_amp);

    e.data[0] = static_cast<float>(d0);
    e.data[1] = static_cast<float>(d1);
    e.data[2] = static_cast<float>(d2);
}

    void SensorSimulator::ApplyStepCounter(sensors_event_t& e, double dt) {
        (void)dt;

        // First time we see a real hardware counter value, fold it into the
        // base so the spoofed counter continues from the device's real total
        // (apps that diff against a previous reading stay consistent).
        if (!step_base_captured_) {
            step_base_captured_ = true;
            if (e.data[0] > 0.0f) {
                step_base_ += static_cast<double>(e.data[0]);
            }
        }

        // Exact: base + real_elapsed_time * cadence. Monotonic non-decreasing.
        double total = StepsAt(e.timestamp);
        if (total < 0.0) total = 0.0;

        // step counter is cumulative and must never go backwards.
        double floored = std::floor(total);
        if (floored < step_counter_) floored = step_counter_;
        step_counter_ = floored;

        e.data[0] = static_cast<float>(step_counter_);
    }


void SensorSimulator::ApplyStepDetector(sensors_event_t& e, double dt) {
    (void)dt;

    // Surface exactly one detector pulse per whole step that has elapsed in
    // real time since the segment start. Tracked separately from the counter
    // so an app reading both sensors gets a consistent cadence.
    long whole = static_cast<long>(std::floor(StepsAt(e.timestamp)));
    if (whole > detector_emitted_) {
        detector_emitted_ = whole;
        e.data[0] = 1.0f;   // a step occurred
    } else {
        e.data[0] = 0.0f;   // no new step in this event
    }
}

void SensorSimulator::ProcessSensorEvents(sensors_event_t* events, size_t count) {
    if (!events || count == 0) return;
    if (!config_.enable) return;
    if (!initialized_.load()) {
        Init();
    }
    
    int64_t last_ts = events[count - 1].timestamp;
    EnsureInitialized(last_ts);
    
    for (size_t i = 0; i < count; i++) {
        sensors_event_t& e = events[i];
        
        int64_t ts = e.timestamp;
        int64_t delta_ns = ts - last_ts_ns_;
        last_ts_ns_ = ts;
        
        double dt = static_cast<double>(delta_ns) * kNsToSec;
        if (dt < 0.0) dt = 0.0;
        if (dt > kMaxDeltaSec) dt = kMaxDeltaSec;
        
        SmoothStepRate(dt);
        AdvancePhase(dt);

        // Keep the time-based step segment in sync with the smoothed cadence.
        // Rebase (bank steps-so-far, restart the segment clock) whenever the
        // effective cadence drifts, so the exact time-integration uses the
        // current rate without losing or double-counting steps.
        if (std::fabs(static_cast<double>(current_spm_) - static_cast<double>(seg_spm_)) > 0.5) {
            RebaseStepSegment(ts, current_spm_);
        }

        switch (e.type) {
            case TYPE_ACCELEROMETER:
                ApplyAccelerometer(e, dt);
                break;
            case TYPE_GYROSCOPE:
                ApplyGyroscope(e, dt);
                break;
            case TYPE_LINEAR_ACCELERATION:
                ApplyLinearAcceleration(e, dt);
                break;
            case TYPE_STEP_COUNTER:
                ApplyStepCounter(e, dt);
                break;
            case TYPE_STEP_DETECTOR:
                ApplyStepDetector(e, dt);
                break;
            default:
                break;
        }
    }
}

void SensorSimulator::ProcessSensorEvent(sensors_event_t& e) {
    if (!config_.enable) return;
    if (!initialized_.load()) {
        Init();
    }
    
    int64_t ts = e.timestamp;
    int64_t delta_ns = ts - last_ts_ns_;
    last_ts_ns_ = ts;
    
    double dt = static_cast<double>(delta_ns) * kNsToSec;
    if (dt < 0.0) dt = 0.0;
    if (dt > kMaxDeltaSec) dt = kMaxDeltaSec;
    
    SmoothStepRate(dt);
    AdvancePhase(dt);

    if (std::fabs(static_cast<double>(current_spm_) - static_cast<double>(seg_spm_)) > 0.5) {
        RebaseStepSegment(ts, current_spm_);
    }

    switch (e.type) {
        case TYPE_ACCELEROMETER:
            ApplyAccelerometer(e, dt);
            break;
        case TYPE_GYROSCOPE:
            ApplyGyroscope(e, dt);
            break;
        case TYPE_LINEAR_ACCELERATION:
            ApplyLinearAcceleration(e, dt);
            break;
        case TYPE_STEP_COUNTER:
            ApplyStepCounter(e, dt);
            break;
        case TYPE_STEP_DETECTOR:
            ApplyStepDetector(e, dt);
            break;
        default:
            break;
    }
}

    bool SensorSimulator::ReloadConfig() {
        FILE* fp = std::fopen(kConfigPath, "re");
        if (!fp) {
            ALOGD("Config file not found: %s", kConfigPath);
            return false;
        }
        
        GaitConfig new_config{};
        new_config.steps_per_minute = 120.0f;
        new_config.mode = GaitMode::Walk;
        new_config.scheme = SimScheme::Fourier;
        new_config.enable = true;
        
        char line[256];
        while (std::fgets(line, sizeof(line), fp)) {
            char* nl = std::strchr(line, '\n');
            if (nl) *nl = '\0';
            if (line[0] == '\0') continue;
            
            char key[64] = {};
            char val[128] = {};
            if (std::sscanf(line, "%63[^=]=%127s", key, val) != 2) continue;
            
            if (std::strcmp(key, "steps_per_minute") == 0) {
                float spm = std::strtof(val, nullptr);
                if (spm > 0.0f) new_config.steps_per_minute = spm;
            } else if (std::strcmp(key, "mode") == 0) {
                if (std::strcmp(val, "walk") == 0) new_config.mode = GaitMode::Walk;
                else if (std::strcmp(val, "run") == 0) new_config.mode = GaitMode::Run;
                else if (std::strcmp(val, "fast_run") == 0) new_config.mode = GaitMode::FastRun;
            } else if (std::strcmp(key, "scheme") == 0) {
                if (std::strcmp(val, "fourier") == 0) new_config.scheme = SimScheme::Fourier;
                else if (std::strcmp(val, "sine_noise") == 0) new_config.scheme = SimScheme::SineNoise;
            } else if (std::strcmp(key, "enable") == 0) {
                new_config.enable = (std::atoi(val) != 0);
            }
        }
        
        std::fclose(fp);
        
        config_ = new_config;
        target_spm_ = config_.steps_per_minute;
        
        ALOGI("Config reloaded: spm=%.2f, mode=%d, scheme=%d, enable=%d",
              config_.steps_per_minute, static_cast<int>(config_.mode),
              static_cast<int>(config_.scheme), config_.enable ? 1 : 0);
        
        return true;
    }

}  // namespace gait
