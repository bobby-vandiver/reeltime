package in.reeltime.reel

class Reel {

    String name

    static hasOne = [audience: Audience]

    static constraints = {
        name nullable: false, blank: false, size: 5..25
        audience unique: true
    }
}
