# Poligon İçinde Nokta Tespiti (Seri ve Paralel Yaklaşımlar)

Bu proje, verilen bir noktanın **konkav veya konveks bir poligonun içinde mi yoksa dışında mı olduğunu belirlemeyi** amaçlar. Bu temel geometrik problemi çözmek için hem **seri (tek iş parçacıklı)** hem de **paralel (çok iş parçacıklı)** algoritmalar kullanılmıştır. Uygulama sonunda, iki yaklaşımın doğruluk ve performans (çalışma süresi) açısından karşılaştırması yapılmış, paralel programlamanın hız avantajı analiz edilmiştir.

Projeyi açıkladığım YouTube videosuna [buraya](https://www.youtube.com/watch?v=gGL9bR2IA-Y) tıklayarak ulaşabilirsiniz.

---

## Kullanılan Algoritma: Ray Casting (Işın Yöntemi)

Projede, noktanın poligon içinde olup olmadığını belirlemek için **Ray Casting (Işın Yöntemi)** kullanılmıştır. Temel prensip:

* **Test noktasından sağa doğru yatay bir ışın gönderilir.**
* Bu ışının poligonun kenarlarıyla kaç kez kesiştiği sayılır.
* **Kesişim sayısı tekse nokta içerde, çiftse dışardadır.**

Bu algoritma, hem konkav hem de konveks poligonlar için geçerlidir. Noktanın kenar üzerinde olması durumu kapsam dışıdır.

---

## Paralel Programlama Yaklaşımı

Ray Casting algoritmasındaki her kenar kontrolü bağımsız olduğu için, algoritma paralel işlemeye oldukça uygundur. Java'nın `java.util.concurrent` kütüphanesi kullanılarak çok iş parçacıklı bir yapı oluşturulmuştur.

### Ana Adımlar:

1.  **Görev Tanımlama:** Her poligon kenarı (`Callable<Integer>`) kendi kesişim kontrolünü yapar ve sonuç döner.
2.  **ExecutorService:** Callable görevler bir `ExecutorService` havuzu içinde yönetilir. Bu, thread oluşturma maliyetini düşürür ve kaynakları verimli kullanır.
3.  **Eş Zamanlı Çalışma:** `executor.invokeAll()` metodu ile tüm görevler eş zamanlı başlatılır ve tamamlanmaları beklenir.
4.  **Sonuç Toplama:** Gelen `Future` nesnelerinden kesişim sayıları toplanarak nihai karar verilir.

Bu yaklaşım, özellikle çok kenarlı poligonlarda ciddi performans artışı sağlar.

---

## Seri ve Paralel Yaklaşımların Karşılaştırılması

### Seri Yöntem:

* Tüm kenarlar tek bir thread üzerinde sırayla kontrol edilir.
* Küçük poligonlarda hızlıdır, thread yönetim yükü yoktur.
* Basit ve okunabilirdir.

### Paralel Yöntem:

* Her kenar kontrolü ayrı bir görev olarak eş zamanlı yürütülür.
* Büyük poligonlarda işlem süresini önemli ölçüde azaltır.
* Küçük örneklerde thread havuzu oluşturma maliyeti nedeniyle yavaş kalabilir.
* Daha karmaşık ama ölçeklenebilir bir çözümdür.

Bu karşılaştırma, projenin hangi senaryoda hangi yaklaşımın daha uygun olduğunu belirlemesine yardımcı olmuştur.
