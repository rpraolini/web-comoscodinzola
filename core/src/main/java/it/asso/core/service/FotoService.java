package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.dao.documenti.FotoDAO;
import it.asso.core.model.documenti.Foto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.imgscalr.Scalr;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;

import javax.imageio.ImageIO;

@Service
public class FotoService {

    private static final Logger logger = LoggerFactory.getLogger(FotoService.class);

    @Value("${file.upload.base-path}")
    private String basePath;

    @Value("${app.images.base-url:}")
    private String remoteImageBaseUrl;

    private final FotoDAO fotoDao;

    @Autowired
    private HttpServletRequest request;

    public FotoService(FotoDAO fotoDao) {
        this.fotoDao = fotoDao;
    }

    public List<Foto> getFotoByAnimale(String idAnimale, String tenant) {
        List<Foto> foto = fotoDao.getFotoByIdAnimale(idAnimale);
        if (foto == null) return Collections.emptyList();
        foto.forEach(f -> populateFotoUrl(f, tenant));
        return foto;
    }

    @Transactional
    public void uploadFoto(String idAnimale, MultipartFile file, String didascalia,
                           String idTipoFoto, String pubblica, String tenant, String account) throws IOException {

        // Regola: foto profilo è sempre pubblica
        if (Def.FOTO_PROFILO.equals(idTipoFoto)) {
            pubblica = Def.FOTO_PUBBLICA;
        }

        // 1. Estensione
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        // 2. Progressivo
        int progressivo = fotoDao.getNextProgressivo(idAnimale);

        // 3. Nomi file
        String nomeOriginale  = idAnimale + "_" + progressivo + "." + extension;
        String nomeThumbnail  = idAnimale + "_" + progressivo + "_T." + extension;

        // 4. Path di destinazione
        Path targetDir = Paths.get(basePath, tenant, idAnimale);
        Files.createDirectories(targetDir);

        Path pathOriginale  = targetDir.resolve(nomeOriginale);
        Path pathThumbnail  = targetDir.resolve(nomeThumbnail);

        // 5. Salvataggio foto originale
        Files.copy(file.getInputStream(), pathOriginale, StandardCopyOption.REPLACE_EXISTING);

        // 6. Generazione thumbnail con imgscalr (max 300px)
        BufferedImage imgOriginale = ImageIO.read(pathOriginale.toFile());
        if (imgOriginale != null) {
            BufferedImage thumbnail = Scalr.resize(imgOriginale,
                    Scalr.Method.QUALITY,
                    Scalr.Mode.AUTOMATIC,
                    300, 300);
            ImageIO.write(thumbnail, extension, pathThumbnail.toFile());
        }

        // 7. Popolamento entità
        Foto foto = new Foto();
        foto.setId_animale(idAnimale);
        foto.setNome_file(nomeOriginale);
        foto.setNome_file_t(nomeThumbnail);
        foto.setEstensione(extension);
        foto.setDimensione(String.valueOf(file.getSize() / 1024));
        foto.setPercorso(pathOriginale.toString());
        foto.setDidascalia(didascalia);
        foto.setAccount(account);
        foto.setId_tipo_foto(idTipoFoto != null ? idTipoFoto : "0");
        foto.setPubblica(pubblica != null ? pubblica : "0");

        fotoDao.saveOrUpdate(foto);
    }

    private Foto populateFotoUrl(Foto f, String tenant) {
        String baseUrl = StringUtils.hasText(remoteImageBaseUrl) ? remoteImageBaseUrl
                : ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replacePath(request.getContextPath()).build().toUriString();

        String urlBase = baseUrl + "/images/" + tenant + "/" + f.getId_animale() + "/";

        f.setUrl(urlBase + f.getNome_file());
        if (f.getNome_file_t() != null && !f.getNome_file_t().isEmpty()) {
            f.setUrl_t(urlBase + f.getNome_file_t());
        }
        return f;
    }

    @Transactional
    public void impostaComeProfilo(String idFoto) {
        Foto foto = fotoDao.getFotoById(idFoto);
        if (foto != null) {
            foto.setId_tipo_foto(Def.FOTO_PROFILO);
            foto.setPubblica(Def.FOTO_PUBBLICA); // profilo sempre pubblica
            fotoDao.saveOrUpdate(foto); // updateImmagineProfilo() scala le altre
        }
    }

    @Transactional
    public void togglePubblica(String idFoto, String pubblica) {
        Foto foto = fotoDao.getFotoById(idFoto);
        if (foto == null) return;

        // Regola: foto profilo non può diventare privata
        if (Def.FOTO_PROFILO.equals(foto.getId_tipo_foto()) && "0".equals(pubblica)) {
            throw new IllegalStateException("La foto profilo deve rimanere pubblica.");
        }

        foto.setPubblica(pubblica);
        fotoDao.saveOrUpdate(foto);
    }

    @Transactional
    public void deleteFoto(String idFoto) throws IOException {
        Foto foto = fotoDao.getFotoById(idFoto);
        if (foto != null) {
            if (foto.getPercorso() != null) {
                Path percorso = Paths.get(foto.getPercorso());

                // percorso è sempre il path assoluto completo incluso nome file
                Files.deleteIfExists(percorso);

                // Thumbnail: stessa directory, nome file thumbnail
                if (foto.getNome_file_t() != null && !foto.getNome_file_t().isEmpty()) {
                    Path pathThumbnail = percorso.getParent().resolve(foto.getNome_file_t());
                    Files.deleteIfExists(pathThumbnail);
                }
            }
            fotoDao.delete(idFoto);
        }
    }

    public void serveImmagine(String idFoto, HttpServletResponse response) throws IOException {
        Foto foto = fotoDao.getFotoById(idFoto);
        if (foto == null) { response.sendError(404); return; }
        Path filePath = Paths.get(basePath, foto.getPercorso(), foto.getNome_file());
        File file = filePath.toFile();
        if (!file.exists()) { response.sendError(404); return; }
        response.setContentType(Files.probeContentType(filePath));
        response.setContentLength((int) file.length());
        Files.copy(filePath, response.getOutputStream());
        response.getOutputStream().flush();
    }

}
