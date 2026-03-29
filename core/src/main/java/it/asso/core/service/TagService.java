package it.asso.core.service;

import it.asso.core.dao.tag.TagDAO;
import it.asso.core.model.tag.Tag;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Service
public class TagService {
    private final TagDAO  tagDAO;

    public TagService(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    public List<Tag> getTagsByAnimale(String id) {
        List<Tag> tags = tagDAO.getTagsByIDAnimale(id);
        return tags;
    }

    public List<Tag> getTags() {
        List<Tag> tags = tagDAO.getTags();
        return tags;
    }

    public void assignTag(String idAnimale, String idTag) throws SQLIntegrityConstraintViolationException {
        tagDAO.saveOrUpdateForAnimale(idAnimale, idTag);
    }

    public void removeTag (String idAnimale, String idTag){
        tagDAO.deleteTagForAnimale(idAnimale, idTag);
    }
}
