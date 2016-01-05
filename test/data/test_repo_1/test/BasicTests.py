import unittest

class BasicTests(unittest.TestCase):

  def test_hello(self):
      self.assertEqual('hello'.upper(), 'HELLO')

if __name__ == '__main__':
    unittest.main()