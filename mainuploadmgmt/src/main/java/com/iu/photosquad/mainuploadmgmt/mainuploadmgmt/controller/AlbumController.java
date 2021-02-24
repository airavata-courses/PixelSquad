package com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.controller;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Dto.AlbumModel;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Dto.ShareAlbumModel;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Dto.UserResponse;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Entity.Album;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Entity.Photo;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Entity.User;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Repo.AlbumRepo;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Repo.PhotoRepo;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.Repo.UserRepo;
import com.iu.photosquad.mainuploadmgmt.mainuploadmgmt.service.RestServiceLocator;

@Controller
@CrossOrigin
@RequestMapping(path="/album")
public class AlbumController {
	
	@Autowired
	private AlbumRepo albumrepo;
	
	@Autowired
	private PhotoRepo photorepo;
	
	@Autowired
	private RestServiceLocator api;
	
	@Autowired
	private UserRepo userrepo;
	
	@Autowired
	private KafkaTemplate<String,Object> template;
	
	@PostMapping(path="/create")
	public ResponseEntity<?> create(@RequestBody AlbumModel album) {
		HashMap<String,String> createresponse = new HashMap<>();
		try {	
			
			if(albumrepo.findByAlbumname(album.getName()) != null && album.getName().equals((String)albumrepo.findByAlbumname(album.getName()).getAlbumname())){
				createresponse.put("album", HttpStatus.ALREADY_REPORTED.toString()); 
			}
			else {
			List<String> tgs = new ArrayList<>();
			Album ab = new Album();
			ab.setCreatedAt(LocalDate.now());
			ab.setUpdatedAt(LocalDate.now());
			ab.setSize(0);
		    ab.setDescription(album.getDescription());
		    ab.setCountOfPhotos(0);
		    ab.setTags(tgs);
		    ab.setAlbumname(album.getName());
		    ab.setSharedpriveledges(album.getSharedpriveledges());
		    ab.setPhotos(new ArrayList<>());
		    User u = findOrAddUser(album.getOwner());
		    System.out.println("method returns" + u);
		    Set<User> st = new HashSet<User>(); 
		    st.add(u);
		    ab.setUsers(st);
		    albumrepo.save(ab);
			}
		}
		catch(Exception e){
			System.out.println(e);
			createresponse.put("album", "exception came");
		}
		return ResponseEntity.ok(createresponse);
	}
	
	@PostMapping(path = "/upload")
	public ResponseEntity<?> createNewObjectWithImage(@RequestParam("files") MultipartFile[] files,
		      @RequestParam("albumname") String albumname,@RequestParam("sharedusers") List<String> users,
		      @RequestParam("photoname") String photoname) throws Exception {

		
		Set<Photo> photoset = new HashSet<>();
		
		for(MultipartFile mf : files) {
			Photo pic = new Photo();
			pic.setPhotoname(photoname + String.valueOf((int) Math.random()));
			pic.setAlbum_id(albumrepo.findByAlbumname(albumname).getId());
			pic.setAnnotationtags("hello1");
			pic.setCreatedAt(LocalDate.now());
			pic.setDescription("hello2");
			pic.setSize(0);
			pic.setUpdatedAt(LocalDate.now());
			pic.setData(mf.getBytes());
			photoset.add(pic);
		}

		List<String> usernames = new ArrayList<>();
		
		Set<User> albumusers = new HashSet<User>();
		
		for(String incominguser : users) {
			if(userrepo.findByUsername(incominguser) != null) {
				
				albumusers.add(userrepo.findByUsername(incominguser));
			}
			else {
				
				usernames.add(incominguser);
			}
		}
		if(!usernames.isEmpty()) {
			
			List<UserResponse> lt = api.getUsersFromUserMgmtDB(usernames);
			
			for (UserResponse u : lt) {
				if(userrepo.findByUsername(u.getUsername()) != null) {
					User ul = userrepo.findByUsername(u.getUsername());
					albumusers.add(ul);
				}
				User newuser = new User();
				newuser.setId(u.getId());
				newuser.setFirstname(u.getFirstname());
				newuser.setLastname(u.getLastname());
				newuser.setEmailID(u.getEmailID());
				newuser.setPassword(u.getPassword());
				newuser.setUsername(u.getUsername());
				newuser.setLastUpdated(u.getLastUpdated());
				userrepo.save(newuser);
				albumusers.add(userrepo.findByUsername(u.getUsername()));
				}
		}
		Album parentalbum = albumrepo.findByAlbumname(albumname);

		if(parentalbum.getUsers() != null) {
			parentalbum.getUsers().addAll(albumusers);
		}
		else {
			Set<User> st = new HashSet<User>(); 
		    st.addAll(albumusers);
		    parentalbum.setUsers(st);
		}
		
		parentalbum.getUsers().addAll(albumusers);
		parentalbum.getPhotos().addAll(photoset);

		
		albumrepo.save(parentalbum);
		Album albs = albumrepo.findByAlbumname(parentalbum.getAlbumname());
		template.send("album", albs.toString());
		System.out.println("album sent to google");
		return ResponseEntity.ok("200");
	}
	
	@GetMapping(path="/getphotos")
	public ResponseEntity<?> findAlbumPhotos(@RequestParam String albumname) {
		HashMap<String,List<Photo>> singlealbumresponse = new HashMap<>();

		Album ab = albumrepo.findByAlbumname(albumname);
//		List<Photo> lt = photorepo.findByAlbum(albumname);
//		System.out.println(ab.getPhotos());
		singlealbumresponse.put("photos", ab.getPhotos());
//		System.out.println(ab);
		return ResponseEntity.ok(singlealbumresponse);
		
	}
	
	@GetMapping(path="/locatealbum")
	public ResponseEntity<?> findonealbum(@RequestParam String albumname) {
		HashMap<String,Album> singlealbumresponse = new HashMap<>();
		try {
				Album u = albumrepo.findByAlbumname(albumname);
				System.out.println(u);
//			if(u != null || (u instanceof Album)) {
//				
				singlealbumresponse.put("albums", u);
//			}
		}
		catch(Exception e){
			e.getStackTrace();
		}
		return ResponseEntity.ok(singlealbumresponse);
		
	}
	
	
	@GetMapping(path="/allalbums")
	public ResponseEntity<?> allalbums() {
		HashMap<String,List<Album>> singlealbumresponse = new HashMap<>();
		try {
			List<Album> u = albumrepo.findAll();
			if(u != null || (u instanceof Album)) {
				
				singlealbumresponse.put("albums", u);
			}
		}
		catch(Exception e){
			e.getStackTrace();
		}
		return ResponseEntity.ok(singlealbumresponse);
		
	}
//	
	@GetMapping(path="/allusers")
	public ResponseEntity<?> listusers() {
		HashMap<String,List<User>> usersresponse = new HashMap<>();
		try {
			List<User> u = userrepo.findAll();
			if(u != null || (u instanceof User)) {
				
				usersresponse.put("users", u);
			}
		}
		catch(Exception e){
			e.getStackTrace();
		}
		return ResponseEntity.ok(usersresponse);
		
	}
//	
//	@GetMapping(path="/allphotos")
//	public ResponseEntity<?> listphotos(@RequestParam String albumname) {
//		HashMap<String,Photo>> photoresponse = new HashMap<>();
//		try {
//			List<Photo> u = photorepo.findAll();
//			for(Photo i : u) {
//				if(i.getAlbum().equals(albumname)) {
//					photoresponse.put("photos", i);
//				}
//			}
//		}
//		catch(Exception e){
//			e.getStackTrace();
//		}
//		return ResponseEntity.ok(photoresponse);
//		
//	}
//	
	public User findOrAddUser(String username) throws Exception {
		User newuser = new User();
		if(userrepo.findByUsername(username) != null) {
			User u = userrepo.findByUsername(username);
			return u;
		}
		else {
			List<String> lt = new ArrayList<>();
			lt.add(username);
			List<UserResponse> lt1 = api.getUsersFromUserMgmtDB(lt);
			for(UserResponse rs : lt1) {
				System.out.println("rs" + rs);
				newuser.setId(rs.getId());
				newuser.setFirstname(rs.getFirstname());
				newuser.setLastname(rs.getLastname());
				newuser.setEmailID(rs.getEmailID());
				newuser.setPassword(rs.getPassword());
				newuser.setUsername(rs.getUsername());
				newuser.setLastUpdated(rs.getLastUpdated());
				userrepo.save(newuser);
				return userrepo.findByUsername(newuser.getUsername());
			}
			return null;
		}
		
	}
//	
	@PostMapping(path="/share")
	public ResponseEntity<?> share(@RequestBody ShareAlbumModel album) throws Exception {
		HashMap<String,Album> shareresponse = new HashMap<>();
		
			
			if(albumrepo.findByAlbumname(album.getAlbumname()) != null){
				List<String> usernames = album.getUsernames();
				
				List<User> lt = new ArrayList<>();
				for(String u : album.getUsernames()) {
					lt.add(findOrAddUser(u));
				}
				
				Album abs = albumrepo.findByAlbumname(album.getAlbumname());
				abs.getUsers().addAll(lt);
				albumrepo.save(abs);
			}
			
		shareresponse.put("album", albumrepo.findByAlbumname(album.getAlbumname()));
		return ResponseEntity.ok(shareresponse);
	}
	
	@GetMapping(path="/download")
	public ResponseEntity<?> share(@RequestParam String albumname) throws FileNotFoundException{


		Album ab = albumrepo.findByAlbumname(albumname);

	     return ResponseEntity.ok()
	             .contentType(MediaType.parseMediaType("image/png"))
	             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "hello.png" + "\"")
	             .body(new ByteArrayResource(ab.getPhotos().get(0).getData()));

	
}
}
