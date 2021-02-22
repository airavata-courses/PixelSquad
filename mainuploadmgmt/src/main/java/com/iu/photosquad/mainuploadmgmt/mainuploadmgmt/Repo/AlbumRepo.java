package com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Entity.Album;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Entity.Photo;


public interface AlbumRepo extends JpaRepository<Album,Long>{
	
	Album findByAlbumname(String albumname);
	Optional<Album> findById(Long id);
	void save(Photo pic);
	
}
