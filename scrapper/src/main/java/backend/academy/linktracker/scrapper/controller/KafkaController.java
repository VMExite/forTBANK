package backend.academy.linktracker.scrapper.controller;

// use it for tests
// @Slf4j
// @RestController
// @RequestMapping("/kafka")
// @RequiredArgsConstructor
// public class KafkaController {
//    private final KafkaMessageSender kafkaProducer;
//
//    @PostMapping
//    public ResponseEntity<@NonNull LinkUpdateMessage> sendMessage(@RequestBody LinkUpdateMessage linkUpdateMessage) {
//        log.info("Received request to send link update message: {}", linkUpdateMessage);
//        kafkaProducer.sendMessage(linkUpdateMessage);
//        return ResponseEntity.ok(linkUpdateMessage);
//    }
// }
