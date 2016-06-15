package in.reeltime.mail

import groovy.transform.ToString

@ToString(includes = ['to', 'from', 'subject', 'body'])
class Email {
    String to
    String from
    String subject
    String body
}
