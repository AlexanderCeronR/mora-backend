package mora.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Value("${cloudinary.cloud.name}")
    private String cloudName;

    @Value("${cloudinary.api.key}")
    private String apiKey;

    @Value("${cloudinary.api.secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String subirImagen(MultipartFile file) throws IOException {
        Map resultado = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "mora/productos",
                        "transformation", ObjectUtils.asMap(
                                "width", 800,
                                "height", 800,
                                "crop", "fill",
                                "quality", "auto"
                        )
                )
        );
        return (String) resultado.get("secure_url");
    }

    public void eliminarImagen(String url) throws IOException {
        // Extrae el public_id de la URL
        String publicId = url
                .substring(url.lastIndexOf("mora/productos/"))
                .replace(".jpg", "")
                .replace(".png", "")
                .replace(".jpeg", "")
                .replace(".webp", "");

        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}