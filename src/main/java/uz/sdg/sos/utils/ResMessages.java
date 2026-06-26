package uz.sdg.sos.utils;

import java.util.Date;

public interface ResMessages {
    Date date = new Date();
    String SERVER_ERROR = "Xatolik yuz berdi ";
    String EXISTENT_PHONE_NUM = "Bunday raqam mavjud ";
    String SUCCESS = "Yaxshi ";
    String NOT_ADMIN = "Siz admin emassiz";
    String DELETE = "O'chirildi ";
    String USER_NOT_FOUND = "Foydalanuvchi topilmadi";
    String OBJECT_NOT_FOUND = "Ma'lumot topilmadi . ";
    String NOT_CREATOR = "Siz ma'lumot yaratuvchisi emassiz .";
    String OBJECT_IS_NULL = "Bo'sh qiymat qabul qilinmaydi";
    String SMS_TOKEN_EDITED = "SMS tokeni almashtirildi";
    String FILE_FORMAT_ERR = "Fayl formati to'g'ri kelmadi. 'jpg' 'gif' 'png' 'jpeg'  'JPG'  'JPEG' 'HEIC' faqat shu formatlarni qabul qilinadi.";


    // error )
    String USER_NOT_ACTIVE = "Faol foydalanuvchi emassiz .";
    String EXIT_LOGIN_COUNT = "Siz bugungi " + date.getDate() +" - kun uchun 'Tizimga kirish' urunishingiz tugadi . Iltimos ertaga yana urunib ko'ring ."; // eroor massage
    String EXIT_LOGIN_BLOCK = "Sizning akkountingiz bugungi kun uchun blok qilindi . Ertaga qayta urubin ko'ring ."; // eroor massage
    String OTP_ERR_MSG_TIME = "Kodni yozish muddati tugagan ."; // eroor massage
    String OTP_ERR_MSG_CODE = "Kod to'gri kelmayabdi ."; // eroor massage
    String EXIT_LOGIN_STEP_ERROR = "Siz ro'yxatdan o'tish uchun oldin Mobile raqamingizni tasdiqlang ."; // eroor massage
    String USER_FOUND = "Bu telefon raqam ro'yxatdan o'tgan"; // eroor massage

}
