package uz.sdg.sos.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.sdg.sos.entity.ClinicApplicationEntity;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.LabEntity;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.*;
import uz.sdg.sos.repository.ClinicApplicationRepository;
import uz.sdg.sos.repository.ClinicRepository;
import uz.sdg.sos.repository.LabRepository;
import uz.sdg.sos.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final LabRepository labRepository;
    private final ClinicApplicationRepository applicationRepository;
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    private final Random rnd = new Random(42);

    public DataLoader(UserRepository userRepository,
                      ClinicRepository clinicRepository,
                      LabRepository labRepository,
                      ClinicApplicationRepository applicationRepository,
                      JdbcTemplate jdbc,
                      @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clinicRepository = clinicRepository;
        this.labRepository = labRepository;
        this.applicationRepository = applicationRepository;
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── Patient names ───────────────────────────────────────────────────────
    private static final String[] PATIENTS = {
        "Alisher Valiyev", "Bobur Rahimov", "Doniyor Toshmatov", "Eldor Xasanov",
        "Farrux Mirzayev", "Husan Karimov", "Jahongir Yusupov", "Kamol Ismoilov",
        "Laziz Nazarov", "Mirzo Ergashev", "Nodirjon Sobirov", "Otabek Holmatov",
        "Pulat Qodirov", "Ravshan Mamatov", "Sardor Abdullayev", "Temur Sultonov",
        "Ulugbek Jurayev", "Vohid Normatov", "Xurshid Baxtiyorov", "Zafar Ibragimov",
        "Aziza Rashidova", "Barno Xoliqova", "Dilnoza Musayeva", "Feruza Nazarova",
        "Gulnora Tursunova", "Hulkar Hasanova", "Iroda Qosimova", "Kamola Yoqubova",
        "Lobar Askarova", "Malika Yusupova", "Nasiba Umarova", "Oydin Sharipova",
        "Parizod Abduraxmonova", "Qunduz Haydarova", "Robiya Aliyeva",
        "Sarvinoz Mirzayeva", "Tabassum Rajabova", "Umida Norova", "Zulfiya Ergasheva"
    };

    // ─── Diagnoses ────────────────────────────────────────────────────────────
    private static final String[] DIAGNOSES = {
        "Arterial gipertenziya 2-darajali",
        "Qandli diabet 2-turi, kompensatsiya holati",
        "O'tkir bronxit, o'rtacha og'irlik",
        "Yurak ishemik kasalligi, stabil stenokardiya",
        "Surunkali gastrit, keskinlashuv davri",
        "Buyrak toshi kasalligi (urolitiaz)",
        "O'tkir pnevmoniya, pastki bo'lak",
        "Infark miokard, o'tkir davr",
        "Miya qon aylanishi o'tkir buzilishi",
        "Surunkali obstruktiv o'pka kasalligi, 2-bosqich",
        "Revmatoid artrit, faol davr",
        "O'tkir appenditsit",
        "Surunkali pankreatit, og'ir shakl",
        "Jigar sirozi, kompensatsiya holati",
        "Qandli diabet 1-turi, dekompensatsiya"
    };

    private static final String[] CONCLUSIONS = {
        "Davolash muvaffaqiyatli yakunlandi, oilaviy shifokor nazorati zarur",
        "Bosim nazorat ostida, dori-darmonlar davom ettirilsin",
        "O'pka holati yaxshilandi, nafas mashqlari tavsiya etiladi",
        "Yurak ritmi me'yorga keldi, haftalik EKG nazorat zarur",
        "Qon qand ko'rsatkichi tushdi, parhez davom ettirilsin",
        "Buyrak funksiyasi qisman tiklandi, ko'proq suv ichilsin",
        "Kasallik remissiyaga kirdi, oylik nazorat tavsiya etiladi",
        "Umumiy holatda yaxshilanish bor, davolash davom ettirilsin",
        "Og'riq sindromlari kamaydi, fizioterapiya kursi tavsiya etiladi",
        "Qon analizi normaga keldi, vitaminlar tayinlandi"
    };

    // ─── Lab test templates [testName, unit, refRange, normalVal, abnormalVal, criticalVal] ──
    private static final String[][] LAB_TESTS = {
        {"HbA1c",              "%",       "4.0-6.0",   "5.3",  "7.1",  "10.5"},
        {"Qon glyukozasi",     "mmol/L",  "3.9-6.1",   "5.0",  "7.8",  "14.2"},
        {"Umumiy xolesterin",  "mmol/L",  "0.0-5.2",   "4.1",  "6.8",  "9.2"},
        {"Gemoglobin",         "g/L",     "120-160",   "138",  "105",  "72"},
        {"Kreatinin",          "mkmol/L", "62-115",    "88",   "145",  "312"},
        {"Umumiy bilirubin",   "mkmol/L", "3.4-17.1",  "12.5", "28.4", "95.0"},
        {"TSH (qalqonsimon)",  "mIU/L",   "0.4-4.0",   "1.8",  "6.2",  "0.05"},
        {"ALT (jigar fermenti)","U/L",    "7-40",      "25",   "78",   "320"},
        {"Leykositlar",        "10^9/L",  "4.0-10.0",  "6.5",  "13.2", "25.8"},
        {"Trombositlar",       "10^9/L",  "150-400",   "230",  "98",   "42"}
    };

    private static final String[] LAB_CONCLUSIONS_NORMAL = {
        "Natija me'yor doirasida, hech qanday chora ko'rish shart emas",
        "Ko'rsatkich normal, parhez va hayot tarzini davom ettirsin",
        "Tahlil natijasi yaxshi, navbatdagi tekshiruv 6 oydan keyin"
    };
    private static final String[] LAB_CONCLUSIONS_ABNORMAL = {
        "Ko'rsatkich biroz oshgan, shifokor bilan maslahat zarur",
        "Natija referens oraliqdan chiqyapti, dori-darmon sozlansin",
        "Dinamikada kuzatish tavsiya etiladi, oylik nazorat zarur"
    };
    private static final String[] LAB_CONCLUSIONS_CRITICAL = {
        "KRITIK: darhol shifokor bilan bog'laning, shoshilinch muolaja zarur",
        "XAVFLI: ko'rsatkich juda yuqori, kasalxonaga yotqizish kerak bo'lishi mumkin",
        "FAVQULODDA: hayot uchun xavfli daraja, tezkor choralar ko'rilsin"
    };

    // ─── Doctor phones for notifications ─────────────────────────────────────
    private static final String[] DOCTOR_PHONES = {
        "+998901001001", "+998901001002", "+998901001003", "+998901001004",
        "+998901001005", "+998901001006", "+998901001007", "+998901001008"
    };

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedAdmin();
        }
        if (userRepository.count() <= 1) {
            log.info("[SEED] Test data yuklanmoqda...");
            List<UserEntity> operators = seedOperatorsAndDoctors();
            List<ClinicEntity> clinics = seedClinics();
            List<LabEntity> labs = seedLabs();
            seedApplications();
            seedMedicalEvents(clinics);
            seedLabEvents(labs);
            log.info("[SEED] Barcha test ma'lumotlari muvaffaqiyatli yuklandi");
        }
    }

    // ─── Admin ───────────────────────────────────────────────────────────────
    private void seedAdmin() {
        UserEntity admin = new UserEntity();
        admin.setFirstName("Jurabek");
        admin.setLastName("Toshbekov");
        admin.setPassword(passwordEncoder.encode("1+1=11"));
        admin.setPhoneNumber("+998994059000");
        admin.setAccountType(AccountTypeEnum.ADMIN);
        admin.setGenderType(GenderType.ERKAK);
        admin.setDateBirth(LocalDate.of(1997, 11, 29));
        admin.setEmail("jurabek@gmail.com");
        userRepository.save(admin);
        log.info("[SEED] Admin yaratildi");
    }

    // ─── Operators + Doctors ─────────────────────────────────────────────────
    private List<UserEntity> seedOperatorsAndDoctors() {
        String enc = passwordEncoder.encode("Test1234");
        List<UserEntity> list = new ArrayList<>();

        String[][] operators = {
            {"Sherzod", "Xoliqov",    "+998711001001", "OPERATOR"},
            {"Madina",  "Yusupova",   "+998711001002", "OPERATOR"},
            {"Jasur",   "Karimov",    "+998711001003", "OPERATOR"},
            {"Nozima",  "Tursunova",  "+998711001004", "OPERATOR"},
            {"Oybek",   "Hasanov",    "+998711001005", "OPERATOR"},
            {"Dilrabo", "Abdullayeva","+998711001006", "OPERATOR"},
            {"Ibrohim", "Rajabov",    "+998711001007", "DOCTOR"},
            {"Mohira",  "Sobirov",    "+998711001008", "DOCTOR"},
            {"Sardor",  "Normatov",   "+998711001009", "DOCTOR"},
            {"Nilufar",  "Qodirov",   "+998711001010", "DOCTOR"},
            {"Behruz",  "Mirzayev",   "+998711001011", "DOCTOR"},
            {"Kamola",  "Ismoilova",  "+998711001012", "DOCTOR"},
        };

        for (String[] d : operators) {
            UserEntity u = new UserEntity();
            u.setFirstName(d[0]);
            u.setLastName(d[1]);
            u.setPhoneNumber(d[2]);
            u.setPassword(enc);
            u.setAccountType(AccountTypeEnum.valueOf(d[3]));
            u.setGenderType(rnd.nextBoolean() ? GenderType.ERKAK : GenderType.AYOL);
            u.setDateBirth(LocalDate.of(1985 + rnd.nextInt(15), 1 + rnd.nextInt(11), 1 + rnd.nextInt(27)));
            list.add(userRepository.save(u));
        }
        log.info("[SEED] {} operator/doktor yaratildi", list.size());
        return list;
    }

    // ─── Clinics ─────────────────────────────────────────────────────────────
    private List<ClinicEntity> seedClinics() {
        LocalDateTime now = LocalDateTime.now();
        List<ClinicEntity> clinics = new ArrayList<>();

        Object[][] data = {
            {"Toshkent 1-Son Poliklinikasi",         "+998712001001", ClinicType.DAVLAT,    now.plusMonths(8)},
            {"Yunusobod 12-Son Poliklinikasi",        "+998712001002", ClinicType.DAVLAT,    now.plusMonths(6)},
            {"Chilonzor 3-Son Poliklinikasi",         "+998712001003", ClinicType.DAVLAT,    now.plusDays(18)},  // expiring soon
            {"Medline Tibbiy Markazi",                "+998712001004", ClinicType.XUSUSIY,   now.plusMonths(10)},
            {"Premium Health Clinic",                 "+998712001005", ClinicType.XUSUSIY,   now.plusMonths(11)},
            {"Shifa Xususiy Klinikasi",               "+998712001006", ClinicType.XUSUSIY,   now.plusDays(22)},  // expiring soon
            {"Tez Yordam 103 — Toshkent markazi",    "+998712001007", ClinicType.TEZYOR_103, now.plusMonths(7)},
            {"Tez Yordam 103 — Yunusobod",            "+998712001008", ClinicType.TEZYOR_103, now.plusMonths(9)},
        };

        for (Object[] d : data) {
            ClinicEntity c = ClinicEntity.builder()
                    .name((String) d[0])
                    .phoneNumber((String) d[1])
                    .clinicType((ClinicType) d[2])
                    .status(ClinicStatus.ACTIVE)
                    .secretKey(UUID.randomUUID().toString().replace("-", ""))
                    .secretKeyGeneratedAt(now.minusMonths(1))
                    .secretKeyExpiresAt((LocalDateTime) d[3])
                    .build();
            clinics.add(clinicRepository.save(c));
        }
        log.info("[SEED] {} klinika yaratildi (2 ta key muddati yaqinlashmoqda)", clinics.size());
        return clinics;
    }

    // ─── Labs ─────────────────────────────────────────────────────────────────
    private List<LabEntity> seedLabs() {
        LocalDateTime now = LocalDateTime.now();
        List<LabEntity> labs = new ArrayList<>();

        // [name, phone, address, status]
        String[][] data = {
            {"Invitro Laboratoriyasi Toshkent",    "+998713001001", "Toshkent, Yunusobod t., Amir Temur ko'chasi 15",    "ACTIVE"},
            {"Medline Diagnostika Lab",             "+998713001002", "Toshkent, Chilonzor t., Bunyodkor ko'chasi 22",     "ACTIVE"},
            {"Central Diagnostics Markazi",         "+998713001003", "Toshkent, Mirzo Ulug'bek t., Movarounnaher 8",      "ACTIVE"},
            {"Bio Lab Toshkent",                    "+998713001004", "Toshkent, Shayxontohur t., Hamza ko'chasi 37",      "ACTIVE"},
            {"Helix Diagnostika Markazi",           "+998713001005", "Toshkent, Mirabad t., Mustaqillik shoh ko'chasi 4", "ACTIVE"},
            {"Andijon Viloyat Diagnostika Lab",     "+998713001006", "Andijon, Navoiy ko'chasi 12",                       "ACTIVE"},
            {"Samarqand Tibbiy Laboratoriya",       "+998713001007", "Samarqand, Registon ko'chasi 5",                    "ACTIVE"},
            {"Namangan Biologik Lab Markazi",       "+998713001008", "Namangan, Uychi ko'chasi 19",                       "ACTIVE"},
            {"Farg'ona Ekspress Diagnostika",       "+998713001009", "Farg'ona, Mustaqillik ko'chasi 31",                 "ACTIVE"},
            {"Buxoro Tibbiy Tahlil Laboratoriyasi", "+998713001010", "Buxoro, Komil Murtazoyev ko'chasi 7",              "ACTIVE"},
            {"Qozon Lab (Yangi Toshkent)",          "+998713001011", "Toshkent, Sergeli t., Yangi shahar 3-blok",         "INACTIVE"},
            {"GeneticsPro Lab",                     "+998713001012", "Toshkent, Beshtepasi massivi, 2-ko'cha 9-uy",       "INACTIVE"},
        };

        for (String[] d : data) {
            ClinicStatus status = ClinicStatus.valueOf(d[3]);
            LabEntity lab = LabEntity.builder()
                    .name(d[0])
                    .phoneNumber(d[1])
                    .address(d[2])
                    .status(status)
                    .secretKey(UUID.randomUUID().toString().replace("-", ""))
                    .secretKeyGeneratedAt(now.minusMonths(1))
                    .secretKeyExpiresAt(status == ClinicStatus.ACTIVE ? now.plusMonths(10) : now.minusDays(5))
                    .build();
            labs.add(labRepository.save(lab));
        }
        log.info("[SEED] {} laboratoriya yaratildi (10 ACTIVE, 2 INACTIVE)", labs.size());
        return labs;
    }

    // ─── Clinic Applications ─────────────────────────────────────────────────
    private void seedApplications() {
        String enc = passwordEncoder.encode("Apply2024");
        Object[][] data = {
            // firstName, lastName, clinicName, phone1, login, status, appType
            {"Nodir",   "Sultonov",  "Andijon 5-Son Poliklinikasi",  "+998751001001", "+998741001001", "PENDING",  "CLINIC"},
            {"Feruz",   "Tojiboyev", "SamarMed Klinikasi",           "+998751001002", "+998741001002", "PENDING",  "CLINIC"},
            {"Gulbahor","Nazarova",  "Namangan Lab Markazi",          "+998751001003", "+998741001003", "PENDING",  "LAB"},
            {"Sanjar",  "Xoliqov",   "Buxoro Tibbiy Lab",            "+998751001004", "+998741001004", "PENDING",  "LAB"},
            {"Dilorom", "Yusupova",  "Farg'ona 2-Poliklinika",        "+998751001005", "+998741001005", "APPROVED", "CLINIC"},
            {"Akbar",   "Rahimov",   "Qoqon Sog'liqni Saqlash",      "+998751001006", "+998741001006", "APPROVED", "CLINIC"},
            {"Zulfiya", "Hasanova",  "Urgench Tibbiy Markazi",       "+998751001007", "+998741001007", "APPROVED", "LAB"},
            {"Botir",   "Mirzayev",  "Qarshi Diagnostika Lab",       "+998751001008", "+998741001008", "APPROVED", "LAB"},
            {"Sarvar",  "Qodirov",   "Jizzax 1-Son Poliklinikasi",   "+998751001009", "+998741001009", "APPROVED", "CLINIC"},
            {"Hulkar",  "Abdullayeva","Termiz Tibbiy Markazi",        "+998751001010", "+998741001010", "REJECTED", "CLINIC"},
            {"Mansur",  "Ergashev",  "Nukus Sog'liqni Saqlash",      "+998751001011", "+998741001011", "REJECTED", "LAB"},
            {"Oydin",   "Tursunova", "Angren Poliklinikasi",         "+998751001012", "+998741001012", "REJECTED", "CLINIC"},
        };

        for (Object[] d : data) {
            ClinicApplicationEntity app = ClinicApplicationEntity.builder()
                    .firstName((String) d[0])
                    .lastName((String) d[1])
                    .clinicName((String) d[2])
                    .phoneNumber1((String) d[3])
                    .login((String) d[4])
                    .password(enc)
                    .status(ClinicApplicationStatus.valueOf((String) d[5]))
                    .applicationType(ApplicationType.valueOf((String) d[6]))
                    .build();
            applicationRepository.save(app);
        }
        log.info("[SEED] 12 ta ariza yaratildi (4 PENDING, 5 APPROVED, 3 REJECTED)");
    }

    // ─── Medical Events (JdbcTemplate — historical dates) ────────────────────
    private void seedMedicalEvents(List<ClinicEntity> clinics) {
        int[] hoursOpts = {1, 2, 4, 8, 24};
        // status weights: 70% NOTIFIED, 20% PENDING, 10% FAILED
        String[] statuses = new String[10];
        Arrays.fill(statuses, 0, 7, "NOTIFIED");
        Arrays.fill(statuses, 7, 9, "PENDING");
        statuses[9] = "FAILED";

        List<Object[]> eventRows = new ArrayList<>();
        List<Object[]> syncRows  = new ArrayList<>();

        for (int i = 0; i < 700; i++) {
            UUID eventId   = UUID.randomUUID();
            UUID clinicId  = clinics.get(rnd.nextInt(clinics.size())).getId();
            String jshshir = String.format("%014d", 10000000000000L + rnd.nextInt(89999999) * 1000L + rnd.nextInt(1000));
            String patient = PATIENTS[rnd.nextInt(PATIENTS.length)];
            String diagnosis  = DIAGNOSES[rnd.nextInt(DIAGNOSES.length)];
            String conclusion = CONCLUSIONS[rnd.nextInt(CONCLUSIONS.length)];
            int hours  = hoursOpts[rnd.nextInt(hoursOpts.length)];
            String status = statuses[rnd.nextInt(statuses.length)];
            String docPhone = DOCTOR_PHONES[rnd.nextInt(DOCTOR_PHONES.length)];

            // Spread over last 30 days; today = ~5% of records
            int daysAgo = i < 35 ? 0 : rnd.nextInt(30);
            LocalDateTime created = LocalDate.now().minusDays(daysAgo)
                    .atStartOfDay().plusHours(rnd.nextInt(20)).plusMinutes(rnd.nextInt(60));
            LocalDateTime notifyAt = created.plusHours(hours);

            eventRows.add(new Object[]{
                eventId, jshshir, patient, diagnosis, conclusion,
                clinicId, "doc-" + rnd.nextInt(500), docPhone, "fcm-token-" + rnd.nextInt(9999),
                hours, notifyAt, status, created, created
            });

            // DMED sync: 85% SUCCESS, 15% FAILED
            boolean syncSuccess = rnd.nextInt(100) < 99;
            syncRows.add(new Object[]{
                UUID.randomUUID(), eventId, jshshir,
                "{\"source\":\"CLINIC\",\"jshshir\":\"" + jshshir + "\"}",
                syncSuccess ? "{\"status\":\"ok\"}" : "{\"error\":\"Connection refused\"}",
                syncSuccess ? 200 : 500,
                syncSuccess ? "SUCCESS" : "FAILED",
                created.plusSeconds(2), created.plusSeconds(2)
            });
        }

        batchInsertEvents(eventRows);
        batchInsertSync(syncRows);
        log.info("[SEED] 700 medical event + 700 DMED sync (99% SUCCESS) yaratildi");
    }

    // ─── Lab Events (JdbcTemplate — historical dates) ────────────────────────
    private void seedLabEvents(List<LabEntity> labs) {
        // flag weights: 50% NORMAL, 30% ABNORMAL, 20% CRITICAL
        String[] flags = new String[10];
        Arrays.fill(flags, 0, 5, "NORMAL");
        Arrays.fill(flags, 5, 8, "ABNORMAL");
        Arrays.fill(flags, 8, 10, "CRITICAL");

        // status weights: 75% NOTIFIED, 15% PENDING, 10% FAILED
        String[] statuses = new String[10];
        Arrays.fill(statuses, 0, 7, "NOTIFIED");
        Arrays.fill(statuses, 7, 9, "PENDING");
        statuses[9] = "FAILED";

        int[] hoursMap = {24, 8, 1}; // NORMAL→24, ABNORMAL→8, CRITICAL→1

        List<Object[]> eventRows = new ArrayList<>();
        List<Object[]> syncRows  = new ArrayList<>();

        for (int i = 0; i < 300; i++) {
            UUID eventId  = UUID.randomUUID();
            UUID labId    = labs.get(rnd.nextInt(labs.size())).getId();
            String jshshir = String.format("%014d", 20000000000000L + rnd.nextInt(89999999) * 1000L + rnd.nextInt(1000));
            String patient = PATIENTS[rnd.nextInt(PATIENTS.length)];
            String flag    = flags[rnd.nextInt(flags.length)];
            String status  = statuses[rnd.nextInt(statuses.length)];
            String docPhone = DOCTOR_PHONES[rnd.nextInt(DOCTOR_PHONES.length)];

            String[] test = LAB_TESTS[rnd.nextInt(LAB_TESTS.length)];
            String testName  = test[0];
            String unit      = test[1];
            String refRange  = test[2];
            String result    = flag.equals("NORMAL") ? test[3] : (flag.equals("ABNORMAL") ? test[4] : test[5]);

            int hours = flag.equals("NORMAL") ? 24 : (flag.equals("ABNORMAL") ? (rnd.nextBoolean() ? 4 : 8) : (rnd.nextBoolean() ? 1 : 2));

            String conclusion = flag.equals("NORMAL")
                    ? LAB_CONCLUSIONS_NORMAL[rnd.nextInt(LAB_CONCLUSIONS_NORMAL.length)]
                    : (flag.equals("ABNORMAL")
                       ? LAB_CONCLUSIONS_ABNORMAL[rnd.nextInt(LAB_CONCLUSIONS_ABNORMAL.length)]
                       : LAB_CONCLUSIONS_CRITICAL[rnd.nextInt(LAB_CONCLUSIONS_CRITICAL.length)]);

            int daysAgo = i < 15 ? 0 : rnd.nextInt(30);
            LocalDateTime created = LocalDate.now().minusDays(daysAgo)
                    .atStartOfDay().plusHours(rnd.nextInt(20)).plusMinutes(rnd.nextInt(60));
            LocalDateTime notifyAt = created.plusHours(hours);

            eventRows.add(new Object[]{
                eventId, jshshir, patient, labId,
                testName, result, unit, refRange,
                flag, hours, conclusion,
                "doc-" + rnd.nextInt(500), docPhone, "fcm-token-" + rnd.nextInt(9999),
                notifyAt, status, created, created
            });

            boolean syncSuccess = rnd.nextInt(100) < 99;
            syncRows.add(new Object[]{
                UUID.randomUUID(), eventId, jshshir,
                "{\"source\":\"LAB\",\"testName\":\"" + testName + "\",\"jshshir\":\"" + jshshir + "\"}",
                syncSuccess ? "{\"status\":\"ok\"}" : "{\"error\":\"Timeout\"}",
                syncSuccess ? 200 : 500,
                syncSuccess ? "SUCCESS" : "FAILED",
                created.plusSeconds(3), created.plusSeconds(3)
            });
        }

        batchInsertLabEvents(eventRows);
        batchInsertSync(syncRows);
        log.info("[SEED] 300 lab event + 300 DMED sync (99% SUCCESS) yaratildi");
    }

    // ─── Batch helpers ────────────────────────────────────────────────────────
    private void batchInsertEvents(List<Object[]> rows) {
        String sql = "INSERT INTO medical_events " +
            "(id, jshshir, patient_full_name, diagnosis, conclusion, clinic_id, " +
            "family_doctor_id, family_doctor_phone, family_doctor_fcm_token, " +
            "hours_until_contact, notify_at, status, created_at, updated_at) " +
            "VALUES (CAST(? AS uuid), ?, ?, ?, ?, CAST(? AS uuid), ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.batchUpdate(sql, rows, 50, (ps, row) -> {
            ps.setObject(1,  row[0].toString());
            ps.setString(2,  (String) row[1]);
            ps.setString(3,  (String) row[2]);
            ps.setString(4,  (String) row[3]);
            ps.setString(5,  (String) row[4]);
            ps.setObject(6,  row[5].toString());
            ps.setString(7,  (String) row[6]);
            ps.setString(8,  (String) row[7]);
            ps.setString(9,  (String) row[8]);
            ps.setInt(10,    (Integer) row[9]);
            ps.setObject(11, row[10]);
            ps.setString(12, (String) row[11]);
            ps.setObject(13, row[12]);
            ps.setObject(14, row[13]);
        });
    }

    private void batchInsertLabEvents(List<Object[]> rows) {
        String sql = "INSERT INTO lab_events " +
            "(id, jshshir, patient_full_name, lab_id, test_name, test_result, unit, reference_range, " +
            "flag, hours_until_contact, conclusion, family_doctor_id, family_doctor_phone, " +
            "family_doctor_fcm_token, notify_at, status, created_at, updated_at) " +
            "VALUES (CAST(? AS uuid), ?, ?, CAST(? AS uuid), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.batchUpdate(sql, rows, 50, (ps, row) -> {
            ps.setObject(1,  row[0].toString());
            ps.setString(2,  (String) row[1]);
            ps.setString(3,  (String) row[2]);
            ps.setObject(4,  row[3].toString());
            ps.setString(5,  (String) row[4]);
            ps.setString(6,  (String) row[5]);
            ps.setString(7,  (String) row[6]);
            ps.setString(8,  (String) row[7]);
            ps.setString(9,  (String) row[8]);
            ps.setInt(10,    (Integer) row[9]);
            ps.setString(11, (String) row[10]);
            ps.setString(12, (String) row[11]);
            ps.setString(13, (String) row[12]);
            ps.setString(14, (String) row[13]);
            ps.setObject(15, row[14]);
            ps.setString(16, (String) row[15]);
            ps.setObject(17, row[16]);
            ps.setObject(18, row[17]);
        });
    }

    private void batchInsertSync(List<Object[]> rows) {
        String sql = "INSERT INTO dmed_sync_log " +
            "(id, medical_event_id, jshshir, request_body, response_body, http_status, status, sent_at, created_at) " +
            "VALUES (CAST(? AS uuid), CAST(? AS uuid), ?, ?, ?, ?, ?, ?, ?)";
        jdbc.batchUpdate(sql, rows, 50, (ps, row) -> {
            ps.setObject(1, row[0].toString());
            ps.setObject(2, row[1].toString());
            ps.setString(3, (String) row[2]);
            ps.setString(4, (String) row[3]);
            ps.setString(5, (String) row[4]);
            ps.setInt(6,    (Integer) row[5]);
            ps.setString(7, (String) row[6]);
            ps.setObject(8, row[7]);
            ps.setObject(9, row[8]);
        });
    }
}
