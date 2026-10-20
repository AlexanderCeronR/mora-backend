package mora.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Value("${cloudinary.cloud.name:default}")
    private String cloudName;

    @Value("${cloudinary.api.key:default}")
    private String apiKey;

    @Value("${cloudinary.api.secret:default}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        String cloudinaryUrl = System.getenv("CLOUDINARY_URL");
        String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");

        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            cloudinary = new Cloudinary(cloudinaryUrl);
            return;
        }

        if (cloudName != null && !cloudName.isBlank()) {
            String apiKey = System.getenv("CLOUDINARY_API_KEY");
            String apiSecret = System.getenv("CLOUDINARY_API_SECRET");
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            System.out.println("Cloudinary configurado con variables separadas");
            return;
        }

        System.out.println("Cloudinary NO configurado");
    }

    public String subirImagen(MultipartFile file) throws IOException {
        if (cloudinary == null) {
            throw new RuntimeException(
                    "Cloudinary no está configurado");
        }
        Map resultado = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "mora/productos",
                        "transformation", new Transformation()
                                .width(800)
                                .height(800)
                                .crop("fill")
                                .quality("auto")
                )
        );
        return (String) resultado.get("secure_url");
    }

    public void eliminarImagen(String url) throws IOException {
        if (cloudinary == null) return;
        String publicId = url
                .substring(url.lastIndexOf("mora/productos/"))
                .replace(".jpg", "")
                .replace(".png", "")
                .replace(".jpeg", "")
                .replace(".webp", "");
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}