# Yemekhane Rezervasyon Sistemi

Bu proje okul yemekhanesi düşünülerek hazırlanmış bir rezervasyon yönetim
sistemidir. İstemci JavaFX kullanılarak geliştirilmiştir.

Projeyi üniversitenin başlarında kodlamıştım. O zaman bu kodlar projenin diğer
parçalarıyla (MySQL sunucusu, projenin minimum gereksinimleri, kağıda çizdiğimiz
tablo ilişkileri planı vs.) bir arada durduğu için neyin ne olduğunu anlamak bu
kadar zor değildi; ancak şu an elimde bu koddan başka bir şey yok ve kod bayağı
kötü yaşlanmış be!

Öte yandan, üniversitede yaptığım en kapsamlı projeler arasında ilk üçe girecek
bir proje bu. Programlamada acemi olduğum yıllarda altından kalkmış olduğum bir iş.
Hala çalışıyor olsa müzelik eser gibi saklayabilirdim bu projeyi, geçmişe açılan bir
pencere gibi bu kodlar. Çalışsaydı... Çalışmayan ve objektif bakıldığında kötü yazılmış
bir yazılım projesinin pek bakılmaya değer bir şey olmadığını düşünüyorum. Bu
nedenle projeyi en azından "restore edip" öyle müzeye koymanın daha uygun olduğuna
karar verdim.

İlk etapta günümüz bilgisayarlarında çalışacak hale getirip<!-- remaster --> 
sonrasında da kod kalitesini iyileştirmeyi ve belki tasarımı modernleştirmeyi<!-- remake -->
amaçlıyorum. Bu süreci de bu *okubeni* dosyasında belgelendireceğim.


## Bölüm 1: Eski Kodu Günümüzde Çalışır Hâle Getirmek

Doğru konfigüre edilmiş bir veritabanı hazırda bulunmadan program normal açılışını
gerçekleştiremiyordu. Kodları fazla deşmeden önce programı açıp işlevini görmek
istiyordum o nedenle de önce veritabanı şemasını çıkarmaya karar verdim. Programı
çalıştırınca aldığım hata mesajlarını inceleyerek ve kodlardaki SQL sorgularını
ayıklayarak gerekli tablo yapılarını çıkardım:

```sql
CREATE TABLE `users` (
    `pid` int NOT NULL,
    `firstname` varchar(50) NOT NULL,
    `lastname` varchar(50) NOT NULL,
    `password` varchar(50) NOT NULL,
    PRIMARY KEY (`pid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;


CREATE TABLE `meal` (
    `mid` INT(10) NOT NULL AUTO_INCREMENT,
    `date` DATE NOT NULL,
    `repast` ENUM('B','L','D') NOT NULL,
    PRIMARY KEY (`mid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
AUTO_INCREMENT=2
;


CREATE TABLE `food` (
    `fid` INT(10) NOT NULL AUTO_INCREMENT,
    `food_name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`fid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
AUTO_INCREMENT=11
;


CREATE TABLE `reservations` (
    `pid` INT(10) NOT NULL,
    `mid` INT(10) NOT NULL,
    `refectory` ENUM('ieylul','yemre') NOT NULL,
    PRIMARY KEY (`pid`, `mid`),
    INDEX `FK_reservations_meal` (`mid`),
    CONSTRAINT `FK_reservations_meal` FOREIGN KEY (`mid`) REFERENCES `meal` (`mid`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `FK_reservations_users` FOREIGN KEY (`pid`) REFERENCES `users` (`pid`) ON UPDATE CASCADE ON DELETE RESTRICT
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;


CREATE TABLE `has_food` (
    `fid` INT(10) NOT NULL,
    `mid` INT(10) NOT NULL,
    PRIMARY KEY (`fid`, `mid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;


CREATE TABLE `has_meal` (
    `pid` INT(10) NOT NULL,
    `mid` INT(10) NOT NULL,
    `refectory` ENUM('ieylul','yemre') NOT NULL,
    PRIMARY KEY (`pid`, `mid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;
```

Kodun ihtiyaç duyduğu tabloları yine kodun içerisinden oluşturan bir betik<!-- ya da "script", her neyse artık.. -->
yazdım. (*bkz.* `org.bozdgn.CreateMockDatabase`) Böylece test etme işlemini de
kolaylaştırmış oldum. (Her seferinde veritabanını elle sıfırlamaya gerek kalmadı.)
Kod üzerinde değişiklik yapmadan önce programın bu aşamada çalıştığına emin olmak
istedim ve birkaç örnek veri girip istemciyi denedim. Beklediğim gibi çalışıyordu.
Hâlâ hatırladığım gibi, aşırı kullanışsız arayüzler ve düzgünce işlenmemiş hatalarla
dolu. Üniversiteli Bora'nın elinden çıkma bir şâheser..

- - -

